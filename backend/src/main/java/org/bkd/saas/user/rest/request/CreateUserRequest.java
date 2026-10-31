package org.bkd.saas.user.rest.request;

import static org.bkd.saas.shared.StringUtils.PASSWORD_MAX_LENGTH;
import static org.bkd.saas.shared.StringUtils.PASSWORD_MIN_LENGTH;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.bkd.saas.shared.StringUtils;

public record CreateUserRequest(@NotBlank @Email String email,
                                @NotBlank @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH) String password) {
  public CreateUserRequest {
    email = StringUtils.normalizeEmail(email);
  }
}
