package org.bkd.saas.user.rest;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bkd.saas.user.dto.RoleEnum;
import org.bkd.saas.user.dto.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserAssertions {

  public static UserDto assertCreateUserOk(ResponseEntity<UserDto> response, String expectedEmail) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isNotNull();
    assertThat(response.getBody().email()).isEqualTo(expectedEmail);
    assertThat(response.getBody().role()).isEqualTo(RoleEnum.ROLE_USER);
    assertThat(response.getBody().enabled()).isFalse();
    assertThat(response.getBody().createdAt()).isNotNull();
    return response.getBody();
  }

  public static UserDto assertMeOk(ResponseEntity<UserDto> response, String expectedEmail) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isNotNull();
    assertThat(response.getBody().email()).isEqualTo(expectedEmail);
    assertThat(response.getBody().role()).isEqualTo(RoleEnum.ROLE_USER);
    assertThat(response.getBody().enabled()).isTrue();
    assertThat(response.getBody().createdAt()).isNotNull();
    return response.getBody();
  }

  public static void assertUpdateUserPasswordOk(ResponseEntity<Void> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();
  }
}
