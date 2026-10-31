package org.bkd.saas.security.rest;

import static org.bkd.saas.security.rest.Routes.LOGIN;
import static org.bkd.saas.security.rest.SecurityAssertions.assertLoginOk;
import static org.bkd.saas.shared.StringUtils.PASSWORD_MAX_LENGTH;
import static org.bkd.saas.shared.StringUtils.PASSWORD_MIN_LENGTH;
import static org.bkd.saas.shared.rest.SharedAssertions.assertKo;

import org.bkd.saas.AbstractIntegrationTest;
import org.bkd.saas.security.exception.InvalidCredentialsException;
import org.bkd.saas.security.rest.request.LoginRequest;
import org.bkd.saas.shared.dto.ErrorDto;
import org.bkd.saas.user.db.AppUserEntity;
import org.bkd.saas.user.db.UserRepository;
import org.bkd.saas.user.dto.UserDto;
import org.bkd.saas.user.exception.DisabledUserException;
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

class LoginIntegrationTests extends AbstractIntegrationTest {
  private static final String EMAIL = "login@example.com";
  private static final String UNNORMALIZED_EMAIL = "  Login@Example.COM ";
  private static final String UNKNOWN_EMAIL = "unknown@example.com";
  private static final String PASSWORD = "password123";
  private static final String WRONG_PASSWORD = "wrong-password";
  private static final String PASSWORD_CHAR = "a";

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void beforeEach() {
    CreateUserRequest request = new CreateUserRequest(EMAIL, PASSWORD);
    userClient.createUserOk(request);
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

  @Test
  void shouldReturnUser_whenCredentialsAreValid() {
    enableUser();
    LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
    ResponseEntity<UserDto> response = securityClient.loginOk(request);
    assertLoginOk(response, EMAIL);
  }

  @Test
  void shouldNormalizeEmail_whenEmailHasCaseAndWhitespace() {
    enableUser();
    LoginRequest request = new LoginRequest(UNNORMALIZED_EMAIL, PASSWORD);
    ResponseEntity<UserDto> response = securityClient.loginOk(request);
    assertLoginOk(response, EMAIL);
  }

  @Test
  void shouldReturn401_whenPasswordIsWrong() {
    enableUser();
    LoginRequest request = new LoginRequest(EMAIL, WRONG_PASSWORD);
    ResponseEntity<ErrorDto> response = securityClient.loginKo(request);
    assertKo(response, HttpStatus.UNAUTHORIZED, LOGIN, InvalidCredentialsException.ERROR_MSG);
  }

  @Test
  void shouldReturn401_whenEmailIsUnknown() {
    LoginRequest request = new LoginRequest(UNKNOWN_EMAIL, PASSWORD);
    ResponseEntity<ErrorDto> response = securityClient.loginKo(request);
    assertKo(response, HttpStatus.UNAUTHORIZED, LOGIN, InvalidCredentialsException.ERROR_MSG);
  }

  @Test
  void shouldReturn403_whenUserIsDisabled() {
    LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
    ResponseEntity<ErrorDto> response = securityClient.loginKo(request);
    assertKo(response, HttpStatus.FORBIDDEN, LOGIN, DisabledUserException.ERROR_MSG + EMAIL);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "not-an-email", "missing-at.example.com", "@example.com", "foo@"})
  void shouldReturn400_whenEmailIsInvalid(String email) {
    LoginRequest request = new LoginRequest(email, PASSWORD);
    ResponseEntity<ErrorDto> response = securityClient.loginKo(request);
    assertKo(response, HttpStatus.BAD_REQUEST, LOGIN);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "        "})
  void shouldReturn400_whenPasswordIsBlank(String password) {
    LoginRequest request = new LoginRequest(EMAIL, password);
    ResponseEntity<ErrorDto> response = securityClient.loginKo(request);
    assertKo(response, HttpStatus.BAD_REQUEST, LOGIN);
  }

  @Test
  void shouldReturn400_whenPasswordIsTooShort() {
    String password = PASSWORD_CHAR.repeat(PASSWORD_MIN_LENGTH - 1);
    LoginRequest request = new LoginRequest(EMAIL, password);
    ResponseEntity<ErrorDto> response = securityClient.loginKo(request);
    assertKo(response, HttpStatus.BAD_REQUEST, LOGIN);
  }

  @Test
  void shouldReturn400_whenPasswordIsTooLong() {
    String password = PASSWORD_CHAR.repeat(PASSWORD_MAX_LENGTH + 1);
    LoginRequest request = new LoginRequest(EMAIL, password);
    ResponseEntity<ErrorDto> response = securityClient.loginKo(request);
    assertKo(response, HttpStatus.BAD_REQUEST, LOGIN);
  }
}
