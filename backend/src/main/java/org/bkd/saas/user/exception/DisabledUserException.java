package org.bkd.saas.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class DisabledUserException extends RuntimeException {
  public static final String ERROR_MSG = "User is disabled: ";

  public DisabledUserException(String email) {
    super(ERROR_MSG + email);
  }
}
