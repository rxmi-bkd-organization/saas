package org.bkd.saas.security.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class TooManyRequestsException extends RuntimeException {
  public static final String ERROR_MSG = "Too many requests. Please try again later.";

  private final long retryAfterSeconds;

  public TooManyRequestsException(long retryAfterSeconds) {
    super(ERROR_MSG);
    this.retryAfterSeconds = retryAfterSeconds;
  }
}
