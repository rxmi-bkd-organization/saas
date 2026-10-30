package org.bkd.saas.security.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bkd.saas.security.rest.Routes.LOGIN;
import static org.bkd.saas.security.rest.Routes.RESET_PASSWORD;
import static org.bkd.saas.security.rest.SecurityAssertions.assertLoginOk;
import static org.bkd.saas.security.rest.SecurityAssertions.assertResetPasswordOk;
import static org.bkd.saas.shared.StringUtils.PASSWORD_MAX_LENGTH;
import static org.bkd.saas.shared.StringUtils.PASSWORD_MIN_LENGTH;
import static org.bkd.saas.shared.rest.SharedAssertions.assertKo;
import static org.bkd.saas.user.rest.Routes.ME;

import java.util.UUID;
import org.bkd.saas.AbstractIntegrationTest;
import org.bkd.saas.security.exception.InvalidCredentialsException;
import org.bkd.saas.security.exception.InvalidTokenException;
import org.bkd.saas.security.rest.request.LoginRequest;
import org.bkd.saas.security.rest.request.PasswordResetRequest;
import org.bkd.saas.security.service.ActivationTokenService;
import org.bkd.saas.security.service.PasswordResetTokenService;
import org.bkd.saas.shared.dto.ErrorDto;
import org.bkd.saas.user.db.AppUserEntity;
import org.bkd.saas.user.db.UserRepository;
import org.bkd.saas.user.dto.UserDto;
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
import org.springframework.security.crypto.password.PasswordEncoder;

class ResetPasswordIT extends AbstractIntegrationTest {
  private static final String EMAIL = "reset-password@example.com";
  private static final String OLD_PASSWORD = "password123";
  private static final String NEW_PASSWORD = "new-password456";
  private static final String MALFORMED_JWT = "not-a-jwt";
  private static final String PASSWORD_CHAR = "a";

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private PasswordResetTokenService passwordResetTokenService;

  @Autowired
  private ActivationTokenService activationTokenService;

  private UUID userId;

  @BeforeEach
  void beforeEach() {
    CreateUserRequest request = new CreateUserRequest(EMAIL, OLD_PASSWORD);
    userId = userClient.createUserOk(request).getBody().id();
    enableUser();
  }

  @AfterEach
  void afterEach() {
    userRepository.deleteAll();
  }

  private void enableUser() {
    AppUserEntity user = userRepository.findByEmail(EMAIL).orElseThrow();
    user.setEnabled(true);
    userRepository.save(user);
  }

  private String createValidJwt() {
    int passwordVersion = userRepository.findByEmail(EMAIL).orElseThrow().getPasswordVersion();
    return passwordResetTokenService.createJwt(userId, passwordVersion);
  }

  @Test
  void shouldReturn204_whenRequestIsValid() {
    PasswordResetRequest request = new PasswordResetRequest(createValidJwt(), NEW_PASSWORD);
    ResponseEntity<Void> response = securityClient.resetPasswordOk(request);
    assertResetPasswordOk(response);
  }

  @Test
  void shouldStoreHashedNewPassword_whenRequestIsValid() {
    PasswordResetRequest request = new PasswordResetRequest(createValidJwt(), NEW_PASSWORD);
    securityClient.resetPasswordOk(request);
    String hash = userRepository.findByEmail(EMAIL).orElseThrow().getPassword();
    assertThat(passwordEncoder.matches(NEW_PASSWORD, hash)).isTrue();
    assertThat(passwordEncoder.matches(OLD_PASSWORD, hash)).isFalse();
  }

  @Test
  void shouldIncrementPasswordVersion_whenRequestIsValid() {
    int versionBefore = userRepository.findByEmail(EMAIL).orElseThrow().getPasswordVersion();
    PasswordResetRequest request = new PasswordResetRequest(createValidJwt(), NEW_PASSWORD);
    securityClient.resetPasswordOk(request);
    int versionAfter = userRepository.findByEmail(EMAIL).orElseThrow().getPasswordVersion();
    assertThat(versionAfter).isEqualTo(versionBefore + 1);
  }

  @Test
  void shouldAllowLoginWithNewPasswordOnly_whenRequestIsValid() {
    PasswordResetRequest request = new PasswordResetRequest(createValidJwt(), NEW_PASSWORD);
    securityClient.resetPasswordOk(request);

    LoginRequest newPasswordLogin = new LoginRequest(EMAIL, NEW_PASSWORD);
    ResponseEntity<UserDto> loginResponse = securityClient.loginOk(newPasswordLogin);
    assertLoginOk(loginResponse, EMAIL);

    LoginRequest oldPasswordLogin = new LoginRequest(EMAIL, OLD_PASSWORD);
    ResponseEntity<ErrorDto> response = securityClient.loginKo(oldPasswordLogin);
    assertKo(response, HttpStatus.UNAUTHORIZED, LOGIN, InvalidCredentialsException.ERROR_MSG);
  }

  @Test
  void shouldInvalidateAllSessions_whenRequestIsValid() {
    LoginRequest loginRequest = new LoginRequest(EMAIL, OLD_PASSWORD);
    String sessionCookie = securityClient.loginAndGetSessionCookie(loginRequest);
    PasswordResetRequest request = new PasswordResetRequest(createValidJwt(), NEW_PASSWORD);
    securityClient.resetPasswordOk(request);
    ResponseEntity<ErrorDto> response = userClient.meKo(sessionCookie);
    assertKo(response, HttpStatus.UNAUTHORIZED, ME);
  }

  @Test
  void shouldReturn401_whenTokenIsAlreadyUsed() {
    String jwt = createValidJwt();
    PasswordResetRequest request = new PasswordResetRequest(jwt, NEW_PASSWORD);
    securityClient.resetPasswordOk(request);
    ResponseEntity<ErrorDto> response = securityClient.resetPasswordKo(request);
    assertKo(response, HttpStatus.UNAUTHORIZED, RESET_PASSWORD, InvalidTokenException.ERROR_MSG + jwt);
  }

  @Test
  void shouldReturn401_whenTokenIsMalformed() {
    PasswordResetRequest request = new PasswordResetRequest(MALFORMED_JWT, NEW_PASSWORD);
    ResponseEntity<ErrorDto> response = securityClient.resetPasswordKo(request);
    assertKo(response, HttpStatus.UNAUTHORIZED, RESET_PASSWORD, InvalidTokenException.ERROR_MSG + MALFORMED_JWT);
  }

  @Test
  void shouldReturn401AndKeepPassword_whenTokenIsSignedWithAnotherKey() {
    String jwt = activationTokenService.createJwt(userId);
    PasswordResetRequest request = new PasswordResetRequest(jwt, NEW_PASSWORD);
    ResponseEntity<ErrorDto> response = securityClient.resetPasswordKo(request);
    assertKo(response, HttpStatus.UNAUTHORIZED, RESET_PASSWORD, InvalidTokenException.ERROR_MSG + jwt);
    String hash = userRepository.findByEmail(EMAIL).orElseThrow().getPassword();
    assertThat(passwordEncoder.matches(OLD_PASSWORD, hash)).isTrue();
  }

  @Test
  void shouldReturn401_whenUserDoesNotExist() {
    String jwt = passwordResetTokenService.createJwt(UUID.randomUUID(), 0);
    PasswordResetRequest request = new PasswordResetRequest(jwt, NEW_PASSWORD);
    ResponseEntity<ErrorDto> response = securityClient.resetPasswordKo(request);
    assertKo(response, HttpStatus.UNAUTHORIZED, RESET_PASSWORD, InvalidTokenException.ERROR_MSG + jwt);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "        "})
  void shouldReturn400_whenJwtIsBlank(String jwt) {
    PasswordResetRequest request = new PasswordResetRequest(jwt, NEW_PASSWORD);
    ResponseEntity<ErrorDto> response = securityClient.resetPasswordKo(request);
    assertKo(response, HttpStatus.BAD_REQUEST, RESET_PASSWORD);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "        "})
  void shouldReturn400_whenNewPasswordIsBlank(String newPassword) {
    PasswordResetRequest request = new PasswordResetRequest(createValidJwt(), newPassword);
    ResponseEntity<ErrorDto> response = securityClient.resetPasswordKo(request);
    assertKo(response, HttpStatus.BAD_REQUEST, RESET_PASSWORD);
  }

  @Test
  void shouldReturn400_whenNewPasswordIsTooShort() {
    String newPassword = PASSWORD_CHAR.repeat(PASSWORD_MIN_LENGTH - 1);
    PasswordResetRequest request = new PasswordResetRequest(createValidJwt(), newPassword);
    ResponseEntity<ErrorDto> response = securityClient.resetPasswordKo(request);
    assertKo(response, HttpStatus.BAD_REQUEST, RESET_PASSWORD);
  }

  @Test
  void shouldReturn400_whenNewPasswordIsTooLong() {
    String newPassword = PASSWORD_CHAR.repeat(PASSWORD_MAX_LENGTH + 1);
    PasswordResetRequest request = new PasswordResetRequest(createValidJwt(), newPassword);
    ResponseEntity<ErrorDto> response = securityClient.resetPasswordKo(request);
    assertKo(response, HttpStatus.BAD_REQUEST, RESET_PASSWORD);
  }
}
