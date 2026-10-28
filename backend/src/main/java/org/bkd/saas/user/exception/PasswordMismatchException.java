package org.bkd.saas.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PasswordMismatchException extends RuntimeException {
  public static final String ERROR_MSG = "Old password is incorrect";

  public PasswordMismatchException() {
    super(ERROR_MSG);
  }
}
