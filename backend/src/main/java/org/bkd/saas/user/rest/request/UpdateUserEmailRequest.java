package org.bkd.saas.user.rest.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.bkd.saas.shared.StringUtils;

public record UpdateUserEmailRequest(@NotBlank @Email String email) {
  public UpdateUserEmailRequest {
    email = StringUtils.normalizeEmail(email);
  }
}
