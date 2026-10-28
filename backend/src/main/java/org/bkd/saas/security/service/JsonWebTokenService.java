package org.bkd.saas.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.bkd.saas.security.exception.InvalidTokenException;

@RequiredArgsConstructor
public class JsonWebTokenService {
  private final SecretKey key;
  private final long expirationInSeconds;

  public String createJwt(UUID subject, Map<String, Object> claims) {
    Instant now = Instant.now();
    Date issuedAt = Date.from(now);
    Date expiration = Date.from(now.plusSeconds(expirationInSeconds));
    JwtBuilder builder = Jwts.builder().subject(subject.toString()).issuedAt(issuedAt).expiration(expiration);
    claims.forEach(builder::claim);
    return builder.signWith(key).compact();
  }

  public Claims readJwt(String jwt) throws InvalidTokenException {
    try {
      return Jwts.parser().verifyWith(key).build().parseSignedClaims(jwt).getPayload();
    }
    catch (JwtException e) {
      throw new InvalidTokenException(jwt);
    }
  }
}
