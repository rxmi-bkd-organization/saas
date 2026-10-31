package org.bkd.saas.security.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.EstimationProbe;
import java.time.Duration;
import java.util.Arrays;
import org.bkd.saas.security.dto.RateLimitPolicyEnum;
import org.bkd.saas.security.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {
  private final boolean enabled;
  private final Cache<String, Bucket> buckets;

  public RateLimitService(@Value("${app.security.rate-limit.enabled}") boolean enabled,
                          @Value("${app.security.rate-limit.bucket-max-size}") long bucketMaxSize) {

    this.enabled = enabled;

    Duration longestPeriod = Arrays.stream(RateLimitPolicyEnum.values())
                                   .map(RateLimitPolicyEnum::getPeriod)
                                   .max(Duration::compareTo)
                                   .orElseThrow();

    this.buckets = Caffeine.newBuilder().maximumSize(bucketMaxSize).expireAfterAccess(longestPeriod).build();
  }

  public void consumeOrThrow(RateLimitPolicyEnum policy, String key) {
    if (enabled) {
      ConsumptionProbe probe = bucket(policy, key).tryConsumeAndReturnRemaining(1);

      if (!probe.isConsumed()) {
        throw new TooManyRequestsException(retryAfterSeconds(probe.getNanosToWaitForRefill()));
      }
    }
  }

  public void checkAvailableOrThrow(RateLimitPolicyEnum policy, String key) {
    if (enabled) {
      Bucket bucket = bucket(policy, key);

      if (bucket.getAvailableTokens() < 1) {
        EstimationProbe probe = bucket.estimateAbilityToConsume(1);
        throw new TooManyRequestsException(retryAfterSeconds(probe.getNanosToWaitForRefill()));
      }
    }
  }

  public void consume(RateLimitPolicyEnum policy, String key) {
    if (enabled) {
      bucket(policy, key).tryConsume(1);
    }
  }

  private Bucket bucket(RateLimitPolicyEnum policy, String key) {
    Bandwidth limit = Bandwidth.builder()
                               .capacity(policy.getCapacity())
                               .refillIntervally(policy.getCapacity(), policy.getPeriod())
                               .build();

    return buckets.get(policy + ":" + key, id -> Bucket.builder().addLimit(limit).build());
  }

  private static long retryAfterSeconds(long nanosToWait) {
    return Math.max(1, Duration.ofNanos(nanosToWait).plusMillis(999).toSeconds());
  }
}
