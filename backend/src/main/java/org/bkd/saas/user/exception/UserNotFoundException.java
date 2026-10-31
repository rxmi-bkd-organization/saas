package org.bkd.saas.user.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
  public static final String ERROR_MSG = "User not found: ";

  public UserNotFoundException(UUID userId) {
    super(ERROR_MSG + userId);
  }

  public UserNotFoundException(String email) {
    super(ERROR_MSG + email);
  }
}
