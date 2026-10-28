package org.bkd.saas.security.rest.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.bkd.saas.shared.StringUtils;

public record ForgotPasswordRequest(@NotBlank @Email String email) {
  public ForgotPasswordRequest {
    email = StringUtils.normalizeEmail(email);
  }
}
