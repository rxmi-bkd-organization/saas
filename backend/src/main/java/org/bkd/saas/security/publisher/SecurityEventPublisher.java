package org.bkd.saas.security.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityEventPublisher {

  private final ApplicationEventPublisher eventPublisher;

  public void publishInteractiveAuthenticationSuccessEvent(Authentication authentication) {
    InteractiveAuthenticationSuccessEvent event = new InteractiveAuthenticationSuccessEvent(authentication,
                                                                                            SecurityEventPublisher.class);
    eventPublisher.publishEvent(event);
  }
}
