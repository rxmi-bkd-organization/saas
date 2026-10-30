package org.bkd.saas.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.bkd.saas.security.dto.RateLimitPolicyEnum;
import org.bkd.saas.security.exception.TooManyRequestsException;
import org.bkd.saas.security.service.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RateLimitServiceUT {
  private static final RateLimitPolicyEnum POLICY = RateLimitPolicyEnum.LOGIN_FAILURES_BY_EMAIL;
  private static final RateLimitPolicyEnum OTHER_POLICY = RateLimitPolicyEnum.SEND_EMAIL_BY_RECIPIENT;
  private static final String KEY = "user@example.com";
  private static final String OTHER_KEY = "other@example.com";
  private static final long BUCKET_MAX_SIZE = 10;

  private RateLimitService rateLimitService;
  private RateLimitService disabledRateLimitService;

  @BeforeEach
  void beforeEach() {
    rateLimitService = new RateLimitService(true, BUCKET_MAX_SIZE);
    disabledRateLimitService = new RateLimitService(false, BUCKET_MAX_SIZE);
  }

  private void exhaust(RateLimitService service, RateLimitPolicyEnum policy, String key) {
    for (int i = 0; i < policy.getCapacity(); i++) {
      service.consumeOrThrow(policy, key);
    }
  }

  private TooManyRequestsException catchTooManyRequests(ThrowingCallable callable) {
    return catchThrowableOfType(TooManyRequestsException.class, callable);
  }

  @Test
  void consumeOrThrow_shouldNotThrow_whenCapacityIsNotExceeded() {
    assertThatCode(() -> exhaust(rateLimitService, POLICY, KEY)).doesNotThrowAnyException();
  }

  @Test
  void consumeOrThrow_shouldThrowTooManyRequests_whenCapacityIsExceeded() {
    exhaust(rateLimitService, POLICY, KEY);
    assertThatThrownBy(() -> rateLimitService.consumeOrThrow(POLICY, KEY)).isInstanceOf(TooManyRequestsException.class)
                                                                          .hasMessage(TooManyRequestsException.ERROR_MSG);
  }

  @Test
  void consumeOrThrow_shouldSetRetryAfterWithinPolicyPeriod_whenCapacityIsExceeded() {
    exhaust(rateLimitService, POLICY, KEY);
    TooManyRequestsException e = catchTooManyRequests(() -> rateLimitService.consumeOrThrow(POLICY, KEY));
    assertThat(e.getRetryAfterSeconds()).isBetween(1L, POLICY.getPeriod().toSeconds());
  }

  @Test
  void consumeOrThrow_shouldTrackKeysIndependently() {
    exhaust(rateLimitService, POLICY, KEY);
    assertThatCode(() -> rateLimitService.consumeOrThrow(POLICY, OTHER_KEY)).doesNotThrowAnyException();
  }

  @Test
  void consumeOrThrow_shouldTrackPoliciesIndependently() {
    exhaust(rateLimitService, POLICY, KEY);
    assertThatCode(() -> rateLimitService.consumeOrThrow(OTHER_POLICY, KEY)).doesNotThrowAnyException();
  }

  @Test
  void consumeOrThrow_shouldNeverThrow_whenDisabled() {
    assertThatCode(() -> {
      for (int i = 0; i <= POLICY.getCapacity() * 2; i++) {
        disabledRateLimitService.consumeOrThrow(POLICY, KEY);
      }
    }).doesNotThrowAnyException();
  }

  @Test
  void checkAvailableOrThrow_shouldNotThrow_whenNothingIsConsumed() {
    assertThatCode(() -> rateLimitService.checkAvailableOrThrow(POLICY, KEY)).doesNotThrowAnyException();
  }

  @Test
  void checkAvailableOrThrow_shouldThrowTooManyRequests_whenBucketIsEmpty() {
    exhaust(rateLimitService, POLICY, KEY);
    assertThatThrownBy(() -> rateLimitService.checkAvailableOrThrow(POLICY, KEY)).isInstanceOf(
                                                                                               TooManyRequestsException.class)
                                                                                 .hasMessage(TooManyRequestsException.ERROR_MSG);
  }

  @Test
  void checkAvailableOrThrow_shouldSetRetryAfterWithinPolicyPeriod_whenBucketIsEmpty() {
    exhaust(rateLimitService, POLICY, KEY);
    TooManyRequestsException e = catchTooManyRequests(() -> rateLimitService.checkAvailableOrThrow(POLICY, KEY));
    assertThat(e.getRetryAfterSeconds()).isBetween(1L, POLICY.getPeriod().toSeconds());
  }

  @Test
  void checkAvailableOrThrow_shouldNotThrow_whenOnlyOtherKeyIsExhausted() {
    exhaust(rateLimitService, POLICY, OTHER_KEY);
    assertThatCode(() -> rateLimitService.checkAvailableOrThrow(POLICY, KEY)).doesNotThrowAnyException();
  }

  @Test
  void checkAvailableOrThrow_shouldNeverThrow_whenDisabled() {
    exhaust(disabledRateLimitService, POLICY, KEY);
    assertThatCode(() -> disabledRateLimitService.checkAvailableOrThrow(POLICY, KEY)).doesNotThrowAnyException();
  }

  @Test
  void consume_shouldNotThrow_whenBucketIsEmpty() {
    exhaust(rateLimitService, POLICY, KEY);
    assertThatCode(() -> rateLimitService.consume(POLICY, KEY)).doesNotThrowAnyException();
  }

  @Test
  void consume_shouldEmptyBucket_whenCapacityIsConsumed() {
    for (int i = 0; i < POLICY.getCapacity(); i++) {
      rateLimitService.consume(POLICY, KEY);
    }

    assertThatThrownBy(() -> rateLimitService.checkAvailableOrThrow(POLICY, KEY)).isInstanceOf(
                                                                                               TooManyRequestsException.class);
  }

  @Test
  void consume_shouldNotConsumeToken_whenDisabled() {
    for (int i = 0; i < POLICY.getCapacity(); i++) {
      disabledRateLimitService.consume(POLICY, KEY);
    }

    assertThatCode(() -> disabledRateLimitService.checkAvailableOrThrow(POLICY, KEY)).doesNotThrowAnyException();
  }
}
