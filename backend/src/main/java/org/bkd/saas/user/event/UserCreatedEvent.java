package org.bkd.saas.user.event;

import java.util.UUID;

public record UserCreatedEvent(UUID userId, String email) {
}
