package org.bkd.saas.security;

import static org.bkd.saas.security.rest.Routes.ENABLE_USER;
import static org.bkd.saas.security.rest.Routes.LOGIN;
import static org.bkd.saas.security.rest.Routes.RESET_PASSWORD;
import static org.bkd.saas.security.rest.Routes.SEND_ACTIVATION_EMAIL;
import static org.bkd.saas.security.rest.Routes.SEND_FORGOTTEN_PASSWORD_EMAIL;

import lombok.RequiredArgsConstructor;
import org.bkd.saas.security.rest.request.EnableUserRequest;
import org.bkd.saas.security.rest.request.ForgotPasswordRequest;
import org.bkd.saas.security.rest.request.LoginRequest;
import org.bkd.saas.security.rest.request.PasswordResetRequest;
import org.bkd.saas.security.rest.request.SendActivationEmailRequest;
import org.bkd.saas.shared.dto.ErrorDto;
import org.bkd.saas.user.dto.UserDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
public class SecurityClient {
  private final RestClient restClient;

  public ResponseEntity<UserDto> loginOk(LoginRequest request) {
    return restClient.post()
                     .uri(LOGIN)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(request)
                     .retrieve()
                     .toEntity(UserDto.class);
  }

  public ResponseEntity<ErrorDto> loginKo(LoginRequest request) {
    return restClient.post()
                     .uri(LOGIN)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(request)
                     .retrieve()
                     .toEntity(ErrorDto.class);
  }

  public ResponseEntity<Void> enableUserOk(EnableUserRequest request) {
    return restClient.post()
                     .uri(ENABLE_USER)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(request)
                     .retrieve()
                     .toBodilessEntity();
  }

  public ResponseEntity<ErrorDto> enableUserKo(EnableUserRequest request) {
    return restClient.post()
                     .uri(ENABLE_USER)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(request)
                     .retrieve()
                     .toEntity(ErrorDto.class);
  }

  public ResponseEntity<Void> sendActivationEmailOk(SendActivationEmailRequest request) {
    return restClient.post()
                     .uri(SEND_ACTIVATION_EMAIL)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(request)
                     .retrieve()
                     .toBodilessEntity();
  }

  public ResponseEntity<ErrorDto> sendActivationEmailKo(SendActivationEmailRequest request) {
    return restClient.post()
                     .uri(SEND_ACTIVATION_EMAIL)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(request)
                     .retrieve()
                     .toEntity(ErrorDto.class);
  }

  public ResponseEntity<Void> sendForgottenPasswordEmailOk(ForgotPasswordRequest request) {
    return restClient.post()
                     .uri(SEND_FORGOTTEN_PASSWORD_EMAIL)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(request)
                     .retrieve()
                     .toBodilessEntity();
  }

  public ResponseEntity<ErrorDto> sendForgottenPasswordEmailKo(ForgotPasswordRequest request) {
    return restClient.post()
                     .uri(SEND_FORGOTTEN_PASSWORD_EMAIL)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(request)
                     .retrieve()
                     .toEntity(ErrorDto.class);
  }

  public ResponseEntity<Void> resetPasswordOk(PasswordResetRequest request) {
    return restClient.post()
                     .uri(RESET_PASSWORD)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(request)
                     .retrieve()
                     .toBodilessEntity();
  }

  public ResponseEntity<ErrorDto> resetPasswordKo(PasswordResetRequest request) {
    return restClient.post()
                     .uri(RESET_PASSWORD)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(request)
                     .retrieve()
                     .toEntity(ErrorDto.class);
  }

  public String loginAndGetSessionCookie(LoginRequest request) {
    ResponseEntity<UserDto> response = loginOk(request);
    String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    return setCookie.split(";")[0];
  }
}
