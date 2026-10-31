package org.bkd.saas.user.exception;

public class PasswordMismatchException extends RuntimeException {
  public static final String ERROR_MSG = "Old password is incorrect";

  public PasswordMismatchException() {
    super(ERROR_MSG);
  }
}
