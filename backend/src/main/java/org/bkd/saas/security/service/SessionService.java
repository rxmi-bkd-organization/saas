package org.bkd.saas.security.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SessionService {
  private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void deleteAllSessions(UUID userId) {
    deleteOtherSessions(userId, null);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void deleteOtherSessions(UUID userId, String currentSessionId) {
    sessionRepository.findByPrincipalName(userId.toString())
                     .keySet()
                     .stream()
                     .filter(sessionId -> !sessionId.equals(currentSessionId))
                     .forEach(sessionRepository::deleteById);
  }
}
