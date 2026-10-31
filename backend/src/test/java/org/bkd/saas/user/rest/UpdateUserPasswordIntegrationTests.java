package org.bkd.saas.user.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bkd.saas.security.rest.Routes.LOGIN;
import static org.bkd.saas.security.rest.SecurityAssertions.assertLoginOk;
import static org.bkd.saas.shared.StringUtils.PASSWORD_MAX_LENGTH;
import static org.bkd.saas.shared.StringUtils.PASSWORD_MIN_LENGTH;
import static org.bkd.saas.shared.rest.SharedAssertions.assertKo;
import static org.bkd.saas.user.exception.PasswordMismatchException.ERROR_MSG;
import static org.bkd.saas.user.rest.Routes.ME;
import static org.bkd.saas.user.rest.Routes.UPDATE_USER_PASSWORD;
import static org.bkd.saas.user.rest.UserAssertions.assertMeOk;
import static org.bkd.saas.user.rest.UserAssertions.assertUpdateUserPasswordOk;

import org.bkd.saas.AbstractIntegrationTest;
import org.bkd.saas.security.exception.InvalidCredentialsException;
import org.bkd.saas.security.rest.request.LoginRequest;
import org.bkd.saas.shared.dto.ErrorDto;
import org.bkd.saas.user.db.AppUserEntity;
import org.bkd.saas.user.db.UserRepository;
import org.bkd.saas.user.dto.UserDto;
import org.bkd.saas.user.rest.request.CreateUserRequest;
import org.bkd.saas.user.rest.request.UpdateUserPasswordRequest;
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

class UpdateUserPasswordIntegrationTests extends AbstractIntegrationTest {
  private static final String EMAIL = "update-password@example.com";
  private static final String OLD_PASSWORD = "password123";
  private static final String NEW_PASSWORD = "new-password456";
  private static final String WRONG_PASSWORD = "wrong-password";
  private static final String PASSWORD_CHAR = "a";

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private String sessionCookie;

  @BeforeEach
  void beforeEach() {
    CreateUserRequest createUserRequest = new CreateUserRequest(EMAIL, OLD_PASSWORD);
    userClient.createUserOk(createUserRequest);
    enableUser();
    sessionCookie = login(OLD_PASSWORD);
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

  private String login(String password) {
    LoginRequest request = new LoginRequest(EMAIL, password);
    return securityClient.loginAndGetSessionCookie(request);
  }

  @Test
  void shouldReturn204_whenRequestIsValid() {
    UpdateUserPasswordRequest request = new UpdateUserPasswordRequest(OLD_PASSWORD, NEW_PASSWORD);
    ResponseEntity<Void> response = userClient.updateUserPasswordOk(sessionCookie, request);
    assertUpdateUserPasswordOk(response);
  }

  @Test
  void shouldStoreHashedNewPassword_whenRequestIsValid() {
    UpdateUserPasswordRequest request = new UpdateUserPasswordRequest(OLD_PASSWORD, NEW_PASSWORD);
    userClient.updateUserPasswordOk(sessionCookie, request);
    String hash = userRepository.findByEmail(EMAIL).orElseThrow().getPassword();
    assertThat(passwordEncoder.matches(NEW_PASSWORD, hash)).isTrue();
    assertThat(passwordEncoder.matches(OLD_PASSWORD, hash)).isFalse();
  }

  @Test
  void shouldIncrementPasswordVersion_whenRequestIsValid() {
    int versionBefore = userRepository.findByEmail(EMAIL).orElseThrow().getPasswordVersion();
    UpdateUserPasswordRequest request = new UpdateUserPasswordRequest(OLD_PASSWORD, NEW_PASSWORD);
    userClient.updateUserPasswordOk(sessionCookie, request);
    int versionAfter = userRepository.findByEmail(EMAIL).orElseThrow().getPasswordVersion();
    assertThat(versionAfter).isEqualTo(versionBefore + 1);
  }

  @Test
  void shouldAllowLoginWithNewPasswordOnly_whenRequestIsValid() {
    UpdateUserPasswordRequest request = new UpdateUserPasswordRequest(OLD_PASSWORD, NEW_PASSWORD);
    userClient.updateUserPasswordOk(sessionCookie, request);

    LoginRequest newPasswordLogin = new LoginRequest(EMAIL, NEW_PASSWORD);
    ResponseEntity<UserDto> loginResponse = securityClient.loginOk(newPasswordLogin);
    assertLoginOk(loginResponse, EMAIL);

    LoginRequest oldPasswordLogin = new LoginRequest(EMAIL, OLD_PASSWORD);
    ResponseEntity<ErrorDto> response = securityClient.loginKo(oldPasswordLogin);
    assertKo(response, HttpStatus.UNAUTHORIZED, LOGIN, InvalidCredentialsException.ERROR_MSG);
  }

  @Test
  void shouldKeepCurrentSession_whenRequestIsValid() {
    UpdateUserPasswordRequest request = new UpdateUserPasswordRequest(OLD_PASSWORD, NEW_PASSWORD);
    userClient.updateUserPasswordOk(sessionCookie, request);
    ResponseEntity<UserDto> response = userClient.meOk(sessionCookie);
    assertMeOk(response, EMAIL);
  }

  @Test
  void shouldInvalidateOtherSessions_whenRequestIsValid() {
    String otherSessionCookie = login(OLD_PASSWORD);
    UpdateUserPasswordRequest request = new UpdateUserPasswordRequest(OLD_PASSWORD, NEW_PASSWORD);
    userClient.updateUserPasswordOk(sessionCookie, request);
    ResponseEntity<ErrorDto> response = userClient.meKo(otherSessionCookie);
    assertKo(response, HttpStatus.UNAUTHORIZED, ME);
  }

  @Test
  void shouldReturn400AndKeepPassword_whenOldPasswordIsWrong() {
    UpdateUserPasswordRequest request = new UpdateUserPasswordRequest(WRONG_PASSWORD, NEW_PASSWORD);
    ResponseEntity<ErrorDto> response = userClient.updateUserPasswordKo(sessionCookie, request);
    assertKo(response, HttpStatus.BAD_REQUEST, UPDATE_USER_PASSWORD, ERROR_MSG);
    String hash = userRepository.findByEmail(EMAIL).orElseThrow().getPassword();
    assertThat(passwordEncoder.matches(OLD_PASSWORD, hash)).isTrue();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "        "})
  void shouldReturn400_whenOldPasswordIsBlank(String oldPassword) {
    UpdateUserPasswordRequest request = new UpdateUserPasswordRequest(oldPassword, NEW_PASSWORD);
    ResponseEntity<ErrorDto> response = userClient.updateUserPasswordKo(sessionCookie, request);
    assertKo(response, HttpStatus.BAD_REQUEST, UPDATE_USER_PASSWORD);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "        "})
  void shouldReturn400_whenNewPasswordIsBlank(String newPassword) {
    UpdateUserPasswordRequest request = new UpdateUserPasswordRequest(OLD_PASSWORD, newPassword);
    ResponseEntity<ErrorDto> response = userClient.updateUserPasswordKo(sessionCookie, request);
    assertKo(response, HttpStatus.BAD_REQUEST, UPDATE_USER_PASSWORD);
  }

  @Test
  void shouldReturn400_whenNewPasswordIsTooShort() {
    String newPassword = PASSWORD_CHAR.repeat(PASSWORD_MIN_LENGTH - 1);
    UpdateUserPasswordRequest request = new UpdateUserPasswordRequest(OLD_PASSWORD, newPassword);
    ResponseEntity<ErrorDto> response = userClient.updateUserPasswordKo(sessionCookie, request);
    assertKo(response, HttpStatus.BAD_REQUEST, UPDATE_USER_PASSWORD);
  }

  @Test
  void shouldReturn400_whenNewPasswordIsTooLong() {
    String newPassword = PASSWORD_CHAR.repeat(PASSWORD_MAX_LENGTH + 1);
    UpdateUserPasswordRequest request = new UpdateUserPasswordRequest(OLD_PASSWORD, newPassword);
    ResponseEntity<ErrorDto> response = userClient.updateUserPasswordKo(sessionCookie, request);
    assertKo(response, HttpStatus.BAD_REQUEST, UPDATE_USER_PASSWORD);
  }

  @Test
  void shouldReturn401_whenNotAuthenticated() {
    UpdateUserPasswordRequest request = new UpdateUserPasswordRequest(OLD_PASSWORD, NEW_PASSWORD);
    ResponseEntity<ErrorDto> response = userClient.updateUserPasswordKo(request);
    assertKo(response, HttpStatus.UNAUTHORIZED, UPDATE_USER_PASSWORD);
  }
}
