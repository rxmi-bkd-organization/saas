package org.bkd.saas.user.exception;

public class DisabledUserException extends RuntimeException {
  public static final String ERROR_MSG = "User is disabled: ";

  public DisabledUserException(String email) {
    super(ERROR_MSG + email);
  }
}
