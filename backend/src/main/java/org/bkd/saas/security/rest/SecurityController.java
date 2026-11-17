package org.bkd.saas.security.rest;

import static org.bkd.saas.security.rest.Routes.CSRF_ENDPOINT;
import static org.bkd.saas.security.rest.Routes.ENABLE_USER_ENDPOINT;
import static org.bkd.saas.security.rest.Routes.LOGIN_ENDPOINT;
import static org.bkd.saas.security.rest.Routes.MOBILE_LOGIN_ENDPOINT;
import static org.bkd.saas.security.rest.Routes.RESET_PASSWORD_ENDPOINT;
import static org.bkd.saas.security.rest.Routes.SEND_ACTIVATION_EMAIL_ENDPOINT;
import static org.bkd.saas.security.rest.Routes.SEND_FORGOTTEN_PASSWORD_EMAIL_ENDPOINT;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bkd.saas.security.dto.AccessTokenDto;
import org.bkd.saas.security.rest.request.EnableUserRequest;
import org.bkd.saas.security.rest.request.ForgotPasswordRequest;
import org.bkd.saas.security.rest.request.LoginRequest;
import org.bkd.saas.security.rest.request.PasswordResetRequest;
import org.bkd.saas.security.rest.request.SendActivationEmailRequest;
import org.bkd.saas.security.service.SecurityService;
import org.bkd.saas.user.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SecurityController {
  private final SecurityService securityService;

  @GetMapping(CSRF_ENDPOINT)
  public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
    // The token is deferred: reading it forces the XSRF-TOKEN cookie to be written on the response.
    csrfToken.getToken();
    return ResponseEntity.noContent().build();
  }

  @PostMapping(LOGIN_ENDPOINT)
  public ResponseEntity<UserDto> login(@Valid @RequestBody LoginRequest request_,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {

    UserDto user = securityService.login(request_.email(), request_.password(), request, response);
    return ResponseEntity.ok().body(user);
  }

  @PostMapping(MOBILE_LOGIN_ENDPOINT)
  public ResponseEntity<AccessTokenDto> loginMobile(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(securityService.loginMobile(request.email(), request.password()));
  }

  @PostMapping(ENABLE_USER_ENDPOINT)
  public ResponseEntity<Void> enableUser(@Valid @RequestBody EnableUserRequest request) {
    securityService.enableUser(request.jwt());
    return ResponseEntity.noContent().build();
  }

  @PostMapping(SEND_ACTIVATION_EMAIL_ENDPOINT)
  public ResponseEntity<Void> sendActivationEmail(@Valid @RequestBody SendActivationEmailRequest request) {
    securityService.sendActivationEmail(request.email());
    return ResponseEntity.noContent().build();
  }

  @PostMapping(SEND_FORGOTTEN_PASSWORD_EMAIL_ENDPOINT)
  public ResponseEntity<Void> sendForgottenPasswordEmail(@Valid @RequestBody ForgotPasswordRequest request) {
    securityService.sendForgottenPasswordEmail(request.email());
    return ResponseEntity.noContent().build();
  }

  @PostMapping(RESET_PASSWORD_ENDPOINT)
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
    securityService.resetPassword(request.jwt(), request.newPassword());
    return ResponseEntity.noContent().build();
  }
}
