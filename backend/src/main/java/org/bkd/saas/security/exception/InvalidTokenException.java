package org.bkd.saas.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidTokenException extends RuntimeException {
  public static final String ERROR_MSG = "Invalid token: ";

  public InvalidTokenException(String token) {
    super(ERROR_MSG + token);
  }
}
