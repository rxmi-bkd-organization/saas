package org.bkd.saas.security.service;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.bkd.saas.security.exception.InvalidTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ActivationTokenService {
  private final JsonWebTokenService jsonWebTokenService;

  public ActivationTokenService(@Value("${app.jwt.activation-token.secret}") String secret,
                                @Value("${app.jwt.activation-token.expiration}") long expirationInSeconds) {

    SecretKey key = hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.jsonWebTokenService = new JsonWebTokenService(key, expirationInSeconds);
  }

  public String createJwt(UUID subject) {
    return jsonWebTokenService.createJwt(subject, Map.of());
  }

  public UUID readSubject(String jwt) throws InvalidTokenException {
    return UUID.fromString(jsonWebTokenService.readJwt(jwt).getSubject());
  }

  public boolean isValidJwt(String jwt) {
    try {
      readSubject(jwt);
      return true;
    }
    catch (InvalidTokenException | IllegalArgumentException e) {
      return false;
    }
  }
}
