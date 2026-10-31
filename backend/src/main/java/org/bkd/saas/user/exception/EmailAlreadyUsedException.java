package org.bkd.saas.user.exception;

public class EmailAlreadyUsedException extends RuntimeException {
  public static final String ERROR_MSG = "Email already used: ";

  public EmailAlreadyUsedException(String email) {
    super(ERROR_MSG + email);
  }
}
