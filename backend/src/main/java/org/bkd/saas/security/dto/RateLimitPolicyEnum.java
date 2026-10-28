package org.bkd.saas.security.dto;

import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RateLimitPolicyEnum {
  LOGIN_BY_IP(20, Duration.ofMinutes(15)),
  LOGIN_FAILURES_BY_EMAIL(5, Duration.ofMinutes(15)),
  SEND_EMAIL_BY_IP(3, Duration.ofHours(1)),
  SEND_EMAIL_BY_RECIPIENT(3, Duration.ofHours(1)),
  SIGNUP_BY_IP(5, Duration.ofHours(1)),
  TOKEN_SUBMISSION_BY_IP(10, Duration.ofMinutes(15));

  private final int capacity;
  private final Duration period;
}
