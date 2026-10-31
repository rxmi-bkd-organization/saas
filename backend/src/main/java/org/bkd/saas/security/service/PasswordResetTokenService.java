package org.bkd.saas.security.service;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.bkd.saas.security.exception.InvalidTokenException;
import org.bkd.saas.user.dto.UserWithPasswordDto;
import org.bkd.saas.user.exception.UserNotFoundException;
import org.bkd.saas.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetTokenService {
  private final JsonWebTokenService jsonWebTokenService;
  private final UserService userService;
  public static final String PASSWORD_VERSION_CLAIM = "pwv";

  public PasswordResetTokenService(@Value("${app.jwt.reset-password-token.secret}") String secret,
                                   @Value("${app.jwt.reset-password-token.expiration}") long expirationInSeconds,
                                   UserService userService) {

    SecretKey key = hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.jsonWebTokenService = new JsonWebTokenService(key, expirationInSeconds);
    this.userService = userService;
  }

  public String createJwt(UUID subject, int passwordVersion) {
    return jsonWebTokenService.createJwt(subject, Map.of(PASSWORD_VERSION_CLAIM, passwordVersion));
  }

  public UUID readSubject(String jwt) {
    Claims claims = jsonWebTokenService.readJwt(jwt);
    return UUID.fromString(claims.getSubject());
  }

  public boolean isValidJwt(String jwt) {
    try {
      Claims claims = jsonWebTokenService.readJwt(jwt);
      UserWithPasswordDto user = userService.readUserWithPassword(UUID.fromString(claims.getSubject()));
      Integer passwordVersion = claims.get(PASSWORD_VERSION_CLAIM, Integer.class);
      return passwordVersion != null && passwordVersion == user.passwordVersion();
    }
    catch (InvalidTokenException | UserNotFoundException e) {
      return false;
    }
  }
}
