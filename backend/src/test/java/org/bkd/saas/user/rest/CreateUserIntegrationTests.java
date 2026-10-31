package org.bkd.saas.user.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bkd.saas.shared.StringUtils.PASSWORD_MAX_LENGTH;
import static org.bkd.saas.shared.StringUtils.PASSWORD_MIN_LENGTH;
import static org.bkd.saas.shared.rest.SharedAssertions.assertKo;
import static org.bkd.saas.user.exception.EmailAlreadyUsedException.ERROR_MSG;
import static org.bkd.saas.user.rest.Routes.CREATE_USER;
import static org.bkd.saas.user.rest.UserAssertions.assertCreateUserOk;

import org.bkd.saas.AbstractIntegrationTest;
import org.bkd.saas.shared.dto.ErrorDto;
import org.bkd.saas.user.db.UserRepository;
import org.bkd.saas.user.dto.UserDto;
import org.bkd.saas.user.rest.request.CreateUserRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

class CreateUserIntegrationTests extends AbstractIntegrationTest {
  private static final String EMAIL = "new@example.com";
  private static final String UNNORMALIZED_EMAIL = "  Foo@Example.COM ";
  private static final String NORMALIZED_EMAIL = "foo@example.com";
  private static final String PASSWORD = "password123";
  private static final String PASSWORD_CHAR = "a";
  private static final String EMAIL_ALREADY_USED = ERROR_MSG + EMAIL;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @AfterEach
  void afterEach() {
    userRepository.deleteAll();
  }

  @Test
  void shouldPersistUser_whenRequestIsValid() {
    CreateUserRequest request = new CreateUserRequest(EMAIL, PASSWORD);
    ResponseEntity<UserDto> response = userClient.createUserOk(request);
    assertCreateUserOk(response, EMAIL);
    boolean isPresent = userRepository.findByEmail(EMAIL).isPresent();
    assertThat(isPresent).isTrue();
  }

  @Test
  void shouldStoreHashedPassword_whenRequestIsValid() {
    CreateUserRequest request = new CreateUserRequest(EMAIL, PASSWORD);
    userClient.createUserOk(request);
    String hash = userRepository.findByEmail(EMAIL).get().getPassword();
    boolean passwordMatches = passwordEncoder.matches(PASSWORD, hash);
    assertThat(passwordMatches).isTrue();
  }

  @Test
  void shouldNormalizeEmail_whenEmailHasCaseAndWhitespace() {
    CreateUserRequest request = new CreateUserRequest(UNNORMALIZED_EMAIL, PASSWORD);
    ResponseEntity<UserDto> response = userClient.createUserOk(request);
    assertCreateUserOk(response, NORMALIZED_EMAIL);
    boolean isPresent = userRepository.findByEmail(NORMALIZED_EMAIL).isPresent();
    assertThat(isPresent).isTrue();
  }

  @Test
  void shouldReturn409_whenEmailAlreadyUsed() {
    CreateUserRequest request = new CreateUserRequest(EMAIL, PASSWORD);
    userClient.createUserOk(request);
    ResponseEntity<ErrorDto> response = userClient.createUserKo(request);
    assertKo(response, HttpStatus.CONFLICT, CREATE_USER, EMAIL_ALREADY_USED);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "not-an-email", "missing-at.example.com", "@example.com", "foo@"})
  void shouldReturn400_whenEmailIsInvalid(String email) {
    CreateUserRequest request = new CreateUserRequest(email, PASSWORD);
    ResponseEntity<ErrorDto> response = userClient.createUserKo(request);
    assertKo(response, HttpStatus.BAD_REQUEST, CREATE_USER);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "        "})
  void shouldReturn400_whenPasswordIsBlank(String password) {
    CreateUserRequest request = new CreateUserRequest(EMAIL, password);
    ResponseEntity<ErrorDto> response = userClient.createUserKo(request);
    assertKo(response, HttpStatus.BAD_REQUEST, CREATE_USER);
  }

  @Test
  void shouldReturn400_whenPasswordIsTooShort() {
    String password = PASSWORD_CHAR.repeat(PASSWORD_MIN_LENGTH - 1);
    CreateUserRequest request = new CreateUserRequest(EMAIL, password);
    ResponseEntity<ErrorDto> response = userClient.createUserKo(request);
    assertKo(response, HttpStatus.BAD_REQUEST, CREATE_USER);
  }

  @Test
  void shouldReturn400_whenPasswordIsTooLong() {
    String password = PASSWORD_CHAR.repeat(PASSWORD_MAX_LENGTH + 1);
    CreateUserRequest request = new CreateUserRequest(EMAIL, password);
    ResponseEntity<ErrorDto> response = userClient.createUserKo(request);
    assertKo(response, HttpStatus.BAD_REQUEST, CREATE_USER);
  }
}