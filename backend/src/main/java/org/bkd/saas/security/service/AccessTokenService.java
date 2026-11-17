package org.bkd.saas.security.service;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.Getter;
import org.bkd.saas.security.exception.InvalidTokenException;
import org.bkd.saas.user.dto.RoleEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenService {
  public static final String ROLE_CLAIM = "role";

  private final JsonWebTokenService jsonWebTokenService;

  @Getter
  private final long expirationInSeconds;

  public AccessTokenService(@Value("${app.jwt.access-token.secret}") String secret,
                            @Value("${app.jwt.access-token.expiration}") long expirationInSeconds) {

    SecretKey key = hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.jsonWebTokenService = new JsonWebTokenService(key, expirationInSeconds);
    this.expirationInSeconds = expirationInSeconds;
  }

  public String createJwt(UUID subject, RoleEnum role) {
    return jsonWebTokenService.createJwt(subject, Map.of(ROLE_CLAIM, role.name()));
  }

  public UUID readSubject(String jwt) throws InvalidTokenException {
    try {
      return UUID.fromString(readJwt(jwt).getSubject());
    }
    catch (IllegalArgumentException | NullPointerException e) {
      throw new InvalidTokenException(jwt);
    }
  }

  public RoleEnum readRole(String jwt) throws InvalidTokenException {
    try {
      return RoleEnum.valueOf(readJwt(jwt).get(ROLE_CLAIM, String.class));
    }
    catch (IllegalArgumentException | NullPointerException e) {
      throw new InvalidTokenException(jwt);
    }
  }

  private Claims readJwt(String jwt) throws InvalidTokenException {
    try {
      return jsonWebTokenService.readJwt(jwt);
    }
    catch (IllegalArgumentException | NullPointerException e) {
      throw new InvalidTokenException(jwt);
    }
  }
}
