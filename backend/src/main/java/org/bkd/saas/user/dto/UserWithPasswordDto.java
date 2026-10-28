package org.bkd.saas.user.dto;

import java.time.Instant;
import java.util.UUID;

public record UserWithPasswordDto(UUID id, String email, RoleEnum role, Instant createdAt, boolean enabled,
                                  int passwordVersion, String password) {
}
