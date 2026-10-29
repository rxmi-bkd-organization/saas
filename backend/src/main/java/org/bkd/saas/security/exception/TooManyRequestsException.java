package org.bkd.saas.security.exception;

import lombok.Getter;

@Getter
public class TooManyRequestsException extends RuntimeException {
  public static final String ERROR_MSG = "Too many requests. Please try again later.";

  private final long retryAfterSeconds;

  public TooManyRequestsException(long retryAfterSeconds) {
    super(ERROR_MSG);
    this.retryAfterSeconds = retryAfterSeconds;
  }
}
