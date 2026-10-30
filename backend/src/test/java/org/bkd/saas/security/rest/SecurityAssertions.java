package org.bkd.saas.security.rest;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bkd.saas.user.dto.RoleEnum;
import org.bkd.saas.user.dto.UserDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityAssertions {
  private static final String SESSION_COOKIE_PREFIX = "SESSION=";

  public static UserDto assertLoginOk(ResponseEntity<UserDto> response, String expectedEmail) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isNotNull();
    assertThat(response.getBody().email()).isEqualTo(expectedEmail);
    assertThat(response.getBody().role()).isEqualTo(RoleEnum.ROLE_USER);
    assertThat(response.getBody().enabled()).isTrue();
    assertThat(response.getBody().createdAt()).isNotNull();
    assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).anyMatch(c -> c.startsWith(SESSION_COOKIE_PREFIX));
    return response.getBody();
  }

  public static void assertEnableUserOk(ResponseEntity<Void> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();
  }

  public static void assertSendActivationEmailOk(ResponseEntity<Void> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();
  }

  public static void assertSendForgottenPasswordEmailOk(ResponseEntity<Void> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();
  }

  public static void assertResetPasswordOk(ResponseEntity<Void> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();
  }
}
