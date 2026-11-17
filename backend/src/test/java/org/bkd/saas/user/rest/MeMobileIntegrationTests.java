package org.bkd.saas.user.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bkd.saas.shared.rest.SharedAssertions.assertKo;
import static org.bkd.saas.user.rest.Routes.MOBILE_ME_ENDPOINT;
import static org.bkd.saas.user.rest.UserAssertions.assertMeOk;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.bkd.saas.AbstractIntegrationTest;
import org.bkd.saas.security.rest.request.LoginRequest;
import org.bkd.saas.security.service.ActivationTokenService;
import org.bkd.saas.shared.dto.ErrorDto;
import org.bkd.saas.user.db.AppUserEntity;
import org.bkd.saas.user.db.UserRepository;
import org.bkd.saas.user.dto.RoleEnum;
import org.bkd.saas.user.dto.UserDto;
import org.bkd.saas.user.rest.request.CreateUserRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class MeMobileIntegrationTests extends AbstractIntegrationTest {
  private static final String EMAIL = "me-mobile@example.com";
  private static final String PASSWORD = "password123";
  private static final String MALFORMED_TOKEN = "not-a-jwt";
  private static final String OTHER_SECRET = "another-secret-that-is-at-least-32-bytes-long-0123456789";
  private static final String ROLE_CLAIM = "role";
  private static final long ONE_HOUR_IN_SECONDS = 3600;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ActivationTokenService activationTokenService;

  @Value("${app.jwt.access-token.secret}")
  private String accessTokenSecret;

  private AppUserEntity user;

  @BeforeEach
  void beforeEach() {
    CreateUserRequest request = new CreateUserRequest(EMAIL, PASSWORD);
    userClient.createUserOk(request);
    user = userRepository.findByEmail(EMAIL).orElseThrow();
    user.setEnabled(true);
    userRepository.save(user);
  }

  @AfterEach
  void afterEach() {
    userRepository.deleteAll();
  }

  private String buildToken(String secret, Instant expiration) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    return Jwts.builder()
               .subject(user.getId().toString())
               .claim(ROLE_CLAIM, RoleEnum.ROLE_USER.name())
               .issuedAt(Date.from(expiration.minusSeconds(ONE_HOUR_IN_SECONDS)))
               .expiration(Date.from(expiration))
               .signWith(key)
               .compact();
  }

  @Test
  void shouldReturnCurrentUser_whenAccessTokenIsValid() {
    LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
    String accessToken = securityClient.loginMobileAndGetAccessToken(request);
    ResponseEntity<UserDto> response = userClient.meMobileOk(accessToken);
    assertMeOk(response, EMAIL);
  }

  @Test
  void shouldNotCreateSession_whenAccessTokenIsValid() {
    LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
    String accessToken = securityClient.loginMobileAndGetAccessToken(request);
    ResponseEntity<UserDto> response = userClient.meMobileOk(accessToken);
    assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).isNull();
  }

  @Test
  void shouldReturn401_whenNotAuthenticated() {
    ResponseEntity<ErrorDto> response = userClient.meMobileKo();
    assertKo(response, HttpStatus.UNAUTHORIZED, MOBILE_ME_ENDPOINT);
  }

  @Test
  void shouldReturn401_whenTokenIsMalformed() {
    ResponseEntity<ErrorDto> response = userClient.meMobileKo(MALFORMED_TOKEN);
    assertKo(response, HttpStatus.UNAUTHORIZED, MOBILE_ME_ENDPOINT);
  }

  @Test
  void shouldReturn401_whenTokenIsExpired() {
    String token = buildToken(accessTokenSecret, Instant.now().minusSeconds(ONE_HOUR_IN_SECONDS));
    ResponseEntity<ErrorDto> response = userClient.meMobileKo(token);
    assertKo(response, HttpStatus.UNAUTHORIZED, MOBILE_ME_ENDPOINT);
  }

  @Test
  void shouldReturn401_whenTokenIsSignedWithAnotherSecret() {
    String token = buildToken(OTHER_SECRET, Instant.now().plusSeconds(ONE_HOUR_IN_SECONDS));
    ResponseEntity<ErrorDto> response = userClient.meMobileKo(token);
    assertKo(response, HttpStatus.UNAUTHORIZED, MOBILE_ME_ENDPOINT);
  }

  @Test
  void shouldReturn401_whenTokenIsAnActivationToken() {
    String token = activationTokenService.createJwt(user.getId());
    ResponseEntity<ErrorDto> response = userClient.meMobileKo(token);
    assertKo(response, HttpStatus.UNAUTHORIZED, MOBILE_ME_ENDPOINT);
  }

  @Test
  void shouldReturn401_whenSessionCookieIsUsedInsteadOfToken() {
    LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
    String sessionCookie = securityClient.loginAndGetSessionCookie(request);
    ResponseEntity<ErrorDto> response = restClient.get()
                                                  .uri(MOBILE_ME_ENDPOINT)
                                                  .header(HttpHeaders.COOKIE, sessionCookie)
                                                  .retrieve()
                                                  .toEntity(ErrorDto.class);
    assertKo(response, HttpStatus.UNAUTHORIZED, MOBILE_ME_ENDPOINT);
  }
}
