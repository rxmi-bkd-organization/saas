package org.bkd.saas.user.exception;

import java.util.UUID;

public class UserAlreadyEnabledException extends RuntimeException {
  public static final String ERROR_MSG = "User is already enabled: ";

  public UserAlreadyEnabledException(String email) {
    super(ERROR_MSG + email);
  }

  public UserAlreadyEnabledException(UUID userId) {
    super(ERROR_MSG + userId);
  }
}
