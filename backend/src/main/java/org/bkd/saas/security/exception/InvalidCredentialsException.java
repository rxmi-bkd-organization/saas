package org.bkd.saas.security.exception;

public class InvalidCredentialsException extends RuntimeException {
  public static final String ERROR_MSG = "Invalid email or password";

  public InvalidCredentialsException() {
    super(ERROR_MSG);
  }
}
