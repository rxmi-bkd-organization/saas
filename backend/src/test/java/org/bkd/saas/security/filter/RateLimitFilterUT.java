package org.bkd.saas.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bkd.saas.security.rest.Routes.ENABLE_USER;
import static org.bkd.saas.security.rest.Routes.LOGIN;
import static org.bkd.saas.security.rest.Routes.RESET_PASSWORD;
import static org.bkd.saas.security.rest.Routes.SEND_ACTIVATION_EMAIL;
import static org.bkd.saas.security.rest.Routes.SEND_FORGOTTEN_PASSWORD_EMAIL;
import static org.bkd.saas.user.rest.Routes.CREATE_USER;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;
import org.bkd.saas.security.dto.RateLimitPolicyEnum;
import org.bkd.saas.security.exception.TooManyRequestsException;
import org.bkd.saas.security.service.RateLimitService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterUT {
  private static final String IP = "10.0.0.1";
  private static final long RETRY_AFTER_SECONDS = 42;

  @Mock
  private RateLimitService rateLimitService;

  @InjectMocks
  private RateLimitFilter filter;

  static Stream<Arguments> limitedPaths() {
    return Stream.of(Arguments.of(LOGIN, RateLimitPolicyEnum.LOGIN_BY_IP),
                     Arguments.of(SEND_FORGOTTEN_PASSWORD_EMAIL, RateLimitPolicyEnum.SEND_EMAIL_BY_IP),
                     Arguments.of(SEND_ACTIVATION_EMAIL, RateLimitPolicyEnum.SEND_EMAIL_BY_IP),
                     Arguments.of(CREATE_USER, RateLimitPolicyEnum.SIGNUP_BY_IP),
                     Arguments.of(RESET_PASSWORD, RateLimitPolicyEnum.TOKEN_SUBMISSION_BY_IP),
                     Arguments.of(ENABLE_USER, RateLimitPolicyEnum.TOKEN_SUBMISSION_BY_IP));
  }

  private static MockHttpServletRequest request(String path) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setServletPath(path);
    request.setRemoteAddr(IP);
    return request;
  }

  @ParameterizedTest
  @MethodSource("limitedPaths")
  void doFilterInternal_shouldConsumeTokenForPolicyAndIp_thenContinueChain(String path,
                                                                           RateLimitPolicyEnum policy) throws Exception {
    MockFilterChain chain = new MockFilterChain();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request(path), response, chain);

    verify(rateLimitService).consumeOrThrow(policy, IP);
    assertThat(chain.getRequest()).isNotNull();
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @ParameterizedTest
  @MethodSource("limitedPaths")
  void doFilterInternal_shouldReturn429WithRetryAfter_whenRateLimitIsExceeded(String path,
                                                                              RateLimitPolicyEnum policy) throws Exception {
    MockFilterChain chain = new MockFilterChain();
    MockHttpServletResponse response = new MockHttpServletResponse();

    TooManyRequestsException tooManyRequestsException = new TooManyRequestsException(RETRY_AFTER_SECONDS);
    doThrow(tooManyRequestsException).when(rateLimitService).consumeOrThrow(policy, IP);

    filter.doFilterInternal(request(path), response, chain);

    assertThat(chain.getRequest()).isNull();
    assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    assertThat(response.getErrorMessage()).isEqualTo(TooManyRequestsException.ERROR_MSG);
    assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo(String.valueOf(RETRY_AFTER_SECONDS));
  }
}
