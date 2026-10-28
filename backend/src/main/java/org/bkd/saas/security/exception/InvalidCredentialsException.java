package org.bkd.saas.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends RuntimeException {
  public static final String ERROR_MSG = "Invalid email or password";

  public InvalidCredentialsException() {
    super(ERROR_MSG);
  }
}
