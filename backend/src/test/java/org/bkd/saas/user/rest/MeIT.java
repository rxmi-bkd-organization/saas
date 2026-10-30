package org.bkd.saas.user.rest;

import static org.bkd.saas.shared.rest.SharedAssertions.assertKo;
import static org.bkd.saas.user.rest.Routes.ME;
import static org.bkd.saas.user.rest.UserAssertions.assertMeOk;

import org.bkd.saas.AbstractIntegrationTest;
import org.bkd.saas.security.rest.request.LoginRequest;
import org.bkd.saas.shared.dto.ErrorDto;
import org.bkd.saas.user.db.AppUserEntity;
import org.bkd.saas.user.db.UserRepository;
import org.bkd.saas.user.dto.UserDto;
import org.bkd.saas.user.rest.request.CreateUserRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class MeIT extends AbstractIntegrationTest {
  private static final String EMAIL = "me@example.com";
  private static final String PASSWORD = "password123";

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void beforeEach() {
    CreateUserRequest request = new CreateUserRequest(EMAIL, PASSWORD);
    userClient.createUserOk(request);
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

  @Test
  void shouldReturnCurrentUser_whenSessionIsValid() {
    LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
    String sessionCookie = securityClient.loginAndGetSessionCookie(request);
    ResponseEntity<UserDto> response = userClient.meOk(sessionCookie);
    assertMeOk(response, EMAIL);
  }

  @Test
  void shouldReturn401_whenNotAuthenticated() {
    ResponseEntity<ErrorDto> response = userClient.meKo();
    assertKo(response, HttpStatus.UNAUTHORIZED, ME);
  }
}
