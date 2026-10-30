package org.bkd.saas.security.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bkd.saas.security.exception.InvalidTokenException.ERROR_MSG;
import static org.bkd.saas.security.rest.Routes.ENABLE_USER;
import static org.bkd.saas.security.rest.SecurityAssertions.assertEnableUserOk;
import static org.bkd.saas.security.rest.SecurityAssertions.assertLoginOk;
import static org.bkd.saas.shared.rest.SharedAssertions.assertKo;

import java.util.UUID;
import org.bkd.saas.AbstractIntegrationTest;
import org.bkd.saas.security.rest.request.EnableUserRequest;
import org.bkd.saas.security.rest.request.LoginRequest;
import org.bkd.saas.security.service.ActivationTokenService;
import org.bkd.saas.security.service.PasswordResetTokenService;
import org.bkd.saas.shared.dto.ErrorDto;
import org.bkd.saas.user.db.UserRepository;
import org.bkd.saas.user.dto.UserDto;
import org.bkd.saas.user.exception.UserAlreadyEnabledException;
import org.bkd.saas.user.exception.UserNotFoundException;
import org.bkd.saas.user.rest.request.CreateUserRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class EnableUserIT extends AbstractIntegrationTest {
  private static final String EMAIL = "enable-user@example.com";
  private static final String PASSWORD = "password123";
  private static final String MALFORMED_JWT = "not-a-jwt";
  private static final int PASSWORD_VERSION = 0;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ActivationTokenService activationTokenService;

  @Autowired
  private PasswordResetTokenService passwordResetTokenService;

  private UUID userId;

  @BeforeEach
  void beforeEach() {
    CreateUserRequest request = new CreateUserRequest(EMAIL, PASSWORD);
    userId = userClient.createUserOk(request).getBody().id();
  }

  @AfterEach
  void afterEach() {
    userRepository.deleteAll();
  }

  @Test
  void shouldReturn204_whenTokenIsValid() {
    String jwt = activationTokenService.createJwt(userId);
    EnableUserRequest request = new EnableUserRequest(jwt);
    ResponseEntity<Void> response = securityClient.enableUserOk(request);
    assertEnableUserOk(response);
  }

  @Test
  void shouldEnableUser_whenTokenIsValid() {
    String jwt = activationTokenService.createJwt(userId);
    EnableUserRequest request = new EnableUserRequest(jwt);
    securityClient.enableUserOk(request);
    boolean enabled = userRepository.findByEmail(EMAIL).orElseThrow().isEnabled();
    assertThat(enabled).isTrue();
  }

  @Test
  void shouldAllowLogin_whenUserIsEnabled() {
    String jwt = activationTokenService.createJwt(userId);
    EnableUserRequest request = new EnableUserRequest(jwt);
    securityClient.enableUserOk(request);
    LoginRequest loginRequest = new LoginRequest(EMAIL, PASSWORD);
    ResponseEntity<UserDto> response = securityClient.loginOk(loginRequest);
    assertLoginOk(response, EMAIL);
  }

  @Test
  void shouldReturn409_whenUserIsAlreadyEnabled() {
    String jwt = activationTokenService.createJwt(userId);
    EnableUserRequest request = new EnableUserRequest(jwt);
    securityClient.enableUserOk(request);
    ResponseEntity<ErrorDto> response = securityClient.enableUserKo(request);
    assertKo(response, HttpStatus.CONFLICT, ENABLE_USER, UserAlreadyEnabledException.ERROR_MSG + userId);
  }

  @Test
  void shouldReturn404_whenUserDoesNotExist() {
    UUID unknownUserId = UUID.randomUUID();
    String jwt = activationTokenService.createJwt(unknownUserId);
    EnableUserRequest request = new EnableUserRequest(jwt);
    ResponseEntity<ErrorDto> response = securityClient.enableUserKo(request);
    assertKo(response, HttpStatus.NOT_FOUND, ENABLE_USER, UserNotFoundException.ERROR_MSG + unknownUserId);
  }

  @Test
  void shouldReturn401_whenTokenIsMalformed() {
    EnableUserRequest request = new EnableUserRequest(MALFORMED_JWT);
    ResponseEntity<ErrorDto> response = securityClient.enableUserKo(request);
    assertKo(response, HttpStatus.UNAUTHORIZED, ENABLE_USER, ERROR_MSG + MALFORMED_JWT);
  }

  @Test
  void shouldReturn401AndKeepUserDisabled_whenTokenIsSignedWithAnotherKey() {
    String jwt = passwordResetTokenService.createJwt(userId, PASSWORD_VERSION);
    EnableUserRequest request = new EnableUserRequest(jwt);
    ResponseEntity<ErrorDto> response = securityClient.enableUserKo(request);
    assertKo(response, HttpStatus.UNAUTHORIZED, ENABLE_USER, ERROR_MSG + jwt);
    boolean enabled = userRepository.findByEmail(EMAIL).orElseThrow().isEnabled();
    assertThat(enabled).isFalse();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "        "})
  void shouldReturn400_whenJwtIsBlank(String jwt) {
    EnableUserRequest request = new EnableUserRequest(jwt);
    ResponseEntity<ErrorDto> response = securityClient.enableUserKo(request);
    assertKo(response, HttpStatus.BAD_REQUEST, ENABLE_USER);
  }
}
