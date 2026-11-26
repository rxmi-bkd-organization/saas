package org.bkd.saas.security.filter;

import static org.bkd.saas.security.rest.Routes.ENABLE_USER_ENDPOINT;
import static org.bkd.saas.security.rest.Routes.LOGIN_ENDPOINT;
import static org.bkd.saas.security.rest.Routes.MOBILE_LOGIN_ENDPOINT;
import static org.bkd.saas.security.rest.Routes.RESET_PASSWORD_ENDPOINT;
import static org.bkd.saas.security.rest.Routes.SEND_ACTIVATION_EMAIL_ENDPOINT;
import static org.bkd.saas.security.rest.Routes.SEND_FORGOTTEN_PASSWORD_EMAIL_ENDPOINT;
import static org.bkd.saas.user.rest.Routes.CREATE_USER_ENDPOINT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bkd.saas.security.dto.RateLimitPolicyEnum;
import org.bkd.saas.security.exception.TooManyRequestsException;
import org.bkd.saas.security.service.RateLimitService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

  private static final Map<String, RateLimitPolicyEnum> POLICY_BY_PATH = Map.of(LOGIN_ENDPOINT,
                                                                                RateLimitPolicyEnum.LOGIN_BY_IP,
                                                                                MOBILE_LOGIN_ENDPOINT,
                                                                                RateLimitPolicyEnum.LOGIN_BY_IP,
                                                                                SEND_FORGOTTEN_PASSWORD_EMAIL_ENDPOINT,
                                                                                RateLimitPolicyEnum.SEND_EMAIL_BY_IP,
                                                                                SEND_ACTIVATION_EMAIL_ENDPOINT,
                                                                                RateLimitPolicyEnum.SEND_EMAIL_BY_IP,
                                                                                CREATE_USER_ENDPOINT,
                                                                                RateLimitPolicyEnum.SIGNUP_BY_IP,
                                                                                RESET_PASSWORD_ENDPOINT,
                                                                                RateLimitPolicyEnum.TOKEN_SUBMISSION_BY_IP,
                                                                                ENABLE_USER_ENDPOINT,
                                                                                RateLimitPolicyEnum.TOKEN_SUBMISSION_BY_IP);

  private final RateLimitService rateLimitService;

  @Override
  public boolean shouldNotFilter(HttpServletRequest request) {
    return !POLICY_BY_PATH.containsKey(request.getServletPath());
  }

  @Override
  public void doFilterInternal(HttpServletRequest request,
                               HttpServletResponse response,
                               FilterChain filterChain) throws ServletException, IOException {
    try {
      rateLimitService.consumeOrThrow(POLICY_BY_PATH.get(request.getServletPath()), request.getRemoteAddr());
    }
    catch (TooManyRequestsException e) {
      response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfterSeconds()));
      response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), e.getMessage());
      return;
    }
    filterChain.doFilter(request, response);
  }
}
