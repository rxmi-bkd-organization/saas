package org.bkd.saas.user.publisher;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bkd.saas.user.event.UserCreatedEvent;
import org.bkd.saas.user.event.UserPasswordUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventPublisher {

  private final ApplicationEventPublisher eventPublisher;

  public void publishUserCreatedEvent(UUID userId, String email) {
    UserCreatedEvent event = new UserCreatedEvent(userId, email);
    eventPublisher.publishEvent(event);
  }

  public void publishUserPasswordUpdatedEvent(UUID userId) {
    UserPasswordUpdatedEvent event = new UserPasswordUpdatedEvent(userId, null);
    eventPublisher.publishEvent(event);
  }

  public void publishUserPasswordUpdatedEvent(UUID userId, String currentSessionId) {
    UserPasswordUpdatedEvent event = new UserPasswordUpdatedEvent(userId, currentSessionId);
    eventPublisher.publishEvent(event);
  }
}
