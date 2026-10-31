package org.bkd.saas.user.rest.request;

import static org.bkd.saas.shared.StringUtils.PASSWORD_MAX_LENGTH;
import static org.bkd.saas.shared.StringUtils.PASSWORD_MIN_LENGTH;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserPasswordRequest(@NotBlank String oldPassword,
                                        @NotBlank @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH) String newPassword) {
}
