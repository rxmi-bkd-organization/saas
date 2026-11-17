package org.bkd.saas.user.rest;

import static org.bkd.saas.user.rest.Routes.CREATE_USER_ENDPOINT;
import static org.bkd.saas.user.rest.Routes.ME_ENDPOINT;
import static org.bkd.saas.user.rest.Routes.MOBILE_ME_ENDPOINT;
import static org.bkd.saas.user.rest.Routes.UPDATE_USER_PASSWORD_ENDPOINT;

import lombok.RequiredArgsConstructor;
import org.bkd.saas.shared.dto.ErrorDto;
import org.bkd.saas.user.dto.UserDto;
import org.bkd.saas.user.rest.request.CreateUserRequest;
import org.bkd.saas.user.rest.request.UpdateUserPasswordRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
public class UserClient {
  private final RestClient restClient;

  public ResponseEntity<UserDto> createUserOk(CreateUserRequest request) {
    return restClient.post()
                     .uri(CREATE_USER_ENDPOINT)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(request)
                     .retrieve()
                     .toEntity(UserDto.class);
  }

  public ResponseEntity<ErrorDto> createUserKo(CreateUserRequest request) {
    return restClient.post()
                     .uri(CREATE_USER_ENDPOINT)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(request)
                     .retrieve()
                     .toEntity(ErrorDto.class);
  }

  public ResponseEntity<UserDto> meOk(String sessionCookie) {
    return restClient.get()
                     .uri(ME_ENDPOINT)
                     .header(HttpHeaders.COOKIE, sessionCookie)
                     .retrieve()
                     .toEntity(UserDto.class);
  }

  public ResponseEntity<ErrorDto> meKo() {
    return restClient.get().uri(ME_ENDPOINT).retrieve().toEntity(ErrorDto.class);
  }

  public ResponseEntity<ErrorDto> meKo(String sessionCookie) {
    return restClient.get()
                     .uri(ME_ENDPOINT)
                     .header(HttpHeaders.COOKIE, sessionCookie)
                     .retrieve()
                     .toEntity(ErrorDto.class);
  }

  public ResponseEntity<UserDto> meMobileOk(String accessToken) {
    return restClient.get()
                     .uri(MOBILE_ME_ENDPOINT)
                     .headers(h -> h.setBearerAuth(accessToken))
                     .retrieve()
                     .toEntity(UserDto.class);
  }

  public ResponseEntity<ErrorDto> meMobileKo(String accessToken) {
    return restClient.get()
                     .uri(MOBILE_ME_ENDPOINT)
                     .headers(h -> h.setBearerAuth(accessToken))
                     .retrieve()
                     .toEntity(ErrorDto.class);
  }

  public ResponseEntity<ErrorDto> meMobileKo() {
    return restClient.get().uri(MOBILE_ME_ENDPOINT).retrieve().toEntity(ErrorDto.class);
  }

  public ResponseEntity<Void> updateUserPasswordOk(String sessionCookie, UpdateUserPasswordRequest request) {
    return restClient.put()
                     .uri(UPDATE_USER_PASSWORD_ENDPOINT)
                     .header(HttpHeaders.COOKIE, sessionCookie)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(request)
                     .retrieve()
                     .toBodilessEntity();
  }

  public ResponseEntity<ErrorDto> updateUserPasswordKo(String sessionCookie, UpdateUserPasswordRequest request) {
    return restClient.put()
                     .uri(UPDATE_USER_PASSWORD_ENDPOINT)
                     .header(HttpHeaders.COOKIE, sessionCookie)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(request)
                     .retrieve()
                     .toEntity(ErrorDto.class);
  }

  public ResponseEntity<ErrorDto> updateUserPasswordKo(UpdateUserPasswordRequest request) {
    return restClient.put()
                     .uri(UPDATE_USER_PASSWORD_ENDPOINT)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(request)
                     .retrieve()
                     .toEntity(ErrorDto.class);
  }
}
