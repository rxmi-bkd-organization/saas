package org.bkd.saas.user.event;

import java.util.UUID;

public record UserPasswordUpdatedEvent(UUID userId, String currentSessionId) {
}
