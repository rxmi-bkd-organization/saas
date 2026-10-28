package org.bkd.saas.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyUsedException extends RuntimeException {
  public static final String ERROR_MSG = "Email already used: ";

  public EmailAlreadyUsedException(String email) {
    super(ERROR_MSG + email);
  }
}
