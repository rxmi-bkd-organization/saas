package org.bkd.saas.security.exception;

public class InvalidTokenException extends RuntimeException {
  public static final String ERROR_MSG = "Invalid token: ";

  public InvalidTokenException(String token) {
    super(ERROR_MSG + token);
  }
}
