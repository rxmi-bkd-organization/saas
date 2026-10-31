package org.bkd.saas.shared.rest;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bkd.saas.shared.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SharedAssertions {
  public static ErrorDto assertKo(ResponseEntity<ErrorDto> response,
                                  HttpStatus expectedStatus,
                                  String expectedPath,
                                  String expectedMessage) {
    assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().timestamp()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(expectedStatus.value());
    assertThat(response.getBody().error()).isEqualTo(expectedStatus.getReasonPhrase());
    assertThat(response.getBody().message()).isEqualTo(expectedMessage);
    assertThat(response.getBody().path()).isEqualTo(expectedPath);
    return response.getBody();
  }

  public static ErrorDto assertKo(ResponseEntity<ErrorDto> response, HttpStatus expectedStatus, String expectedPath) {
    return assertKo(response, expectedStatus, expectedPath, null);
  }
}
