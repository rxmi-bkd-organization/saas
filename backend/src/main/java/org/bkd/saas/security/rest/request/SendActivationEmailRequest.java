package org.bkd.saas.security.rest.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.bkd.saas.shared.StringUtils;

public record SendActivationEmailRequest(@NotBlank @Email String email) {
  public SendActivationEmailRequest {
    email = StringUtils.normalizeEmail(email);
  }
}
