package org.bkd.saas.security.listener;

import lombok.RequiredArgsConstructor;
import org.bkd.saas.security.service.EmailService;
import org.bkd.saas.security.service.SessionService;
import org.bkd.saas.user.event.UserCreatedEvent;
import org.bkd.saas.user.event.UserPasswordUpdatedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class UserEventListener {

  private final EmailService emailService;
  private final SessionService sessionService;

  @TransactionalEventListener
  public void onUserCreatedEvent(UserCreatedEvent event) {
    emailService.sendActivationEmail(event.userId(), event.email());
  }

  @TransactionalEventListener
  public void onUserPasswordUpdatedEvent(UserPasswordUpdatedEvent event) {
    if (event.currentSessionId() == null) {
      sessionService.deleteAllSessions(event.userId());
    }
    else {
      sessionService.deleteOtherSessions(event.userId(), event.currentSessionId());
    }
  }
}
