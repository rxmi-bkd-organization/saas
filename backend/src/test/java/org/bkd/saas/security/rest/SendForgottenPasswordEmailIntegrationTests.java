package org.bkd.saas.security.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bkd.saas.security.rest.Routes.SEND_FORGOTTEN_PASSWORD_EMAIL;
import static org.bkd.saas.security.rest.SecurityAssertions.assertSendForgottenPasswordEmailOk;
import static org.bkd.saas.shared.rest.SharedAssertions.assertKo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.bkd.saas.AbstractIntegrationTest;
import org.bkd.saas.notification.dto.ChannelEnum;
import org.bkd.saas.notification.dto.NotificationDto;
import org.bkd.saas.notification.service.NotificationService;
import org.bkd.saas.security.rest.request.ForgotPasswordRequest;
import org.bkd.saas.security.service.PasswordResetTokenService;
import org.bkd.saas.shared.dto.ErrorDto;
import org.bkd.saas.user.db.AppUserEntity;
import org.bkd.saas.user.db.UserRepository;
import org.bkd.saas.user.exception.UserNotFoundException;
import org.bkd.saas.user.rest.request.CreateUserRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class SendForgottenPasswordEmailIntegrationTests extends AbstractIntegrationTest {
  private static final String EMAIL = "forgotten-password@example.com";
  private static final String UNNORMALIZED_EMAIL = "  Forgotten-Password@Example.COM ";
  private static final String UNKNOWN_EMAIL = "unknown@example.com";
  private static final String PASSWORD = "password123";
  private static final String FORGOTTEN_PASSWORD_SUBJECT = "Forgot your password ?";
  private static final String TOKEN_PARAM = "/reset-password?token=";

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordResetTokenService passwordResetTokenService;

  @MockitoSpyBean
  private NotificationService notificationService;

  @BeforeEach
  void beforeEach() {
    CreateUserRequest request = new CreateUserRequest(EMAIL, PASSWORD);
    userClient.createUserOk(request);
    enableUser();
    clearInvocations(notificationService);
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

  private NotificationDto captureSentEmail() {
    ArgumentCaptor<NotificationDto> captor = ArgumentCaptor.forClass(NotificationDto.class);
    verify(notificationService).sendNotification(eq(ChannelEnum.EMAIL), captor.capture());
    return captor.getValue();
  }

  @Test
  void shouldReturn204_whenUserExists() {
    ForgotPasswordRequest request = new ForgotPasswordRequest(EMAIL);
    ResponseEntity<Void> response = securityClient.sendForgottenPasswordEmailOk(request);
    assertSendForgottenPasswordEmailOk(response);
  }

  @Test
  void shouldSendResetPasswordEmail_whenUserExists() {
    ForgotPasswordRequest request = new ForgotPasswordRequest(EMAIL);
    securityClient.sendForgottenPasswordEmailOk(request);
    NotificationDto email = captureSentEmail();
    assertThat(email.recipient()).isEqualTo(EMAIL);
    assertThat(email.subject()).isEqualTo(FORGOTTEN_PASSWORD_SUBJECT);
    assertThat(email.body()).contains(TOKEN_PARAM);
  }

  @Test
  void shouldSendEmailWithValidToken_whenUserExists() {
    ForgotPasswordRequest request = new ForgotPasswordRequest(EMAIL);
    securityClient.sendForgottenPasswordEmailOk(request);
    String body = captureSentEmail().body();
    String jwt = body.substring(body.indexOf(TOKEN_PARAM) + TOKEN_PARAM.length());
    boolean isValidJwt = passwordResetTokenService.isValidJwt(jwt);
    assertThat(isValidJwt).isTrue();
  }

  @Test
  void shouldSendResetPasswordEmail_whenUserIsDisabled() {
    AppUserEntity user = userRepository.findByEmail(EMAIL).orElseThrow();
    user.setEnabled(false);
    userRepository.save(user);
    ForgotPasswordRequest request = new ForgotPasswordRequest(EMAIL);
    ResponseEntity<Void> response = securityClient.sendForgottenPasswordEmailOk(request);
    assertSendForgottenPasswordEmailOk(response);
    assertThat(captureSentEmail().recipient()).isEqualTo(EMAIL);
  }

  @Test
  void shouldNormalizeEmail_whenEmailHasCaseAndWhitespace() {
    ForgotPasswordRequest request = new ForgotPasswordRequest(UNNORMALIZED_EMAIL);
    ResponseEntity<Void> response = securityClient.sendForgottenPasswordEmailOk(request);
    assertSendForgottenPasswordEmailOk(response);
    assertThat(captureSentEmail().recipient()).isEqualTo(EMAIL);
  }

  @Test
  void shouldReturn404AndSendNothing_whenEmailIsUnknown() {
    ForgotPasswordRequest request = new ForgotPasswordRequest(UNKNOWN_EMAIL);
    ResponseEntity<ErrorDto> response = securityClient.sendForgottenPasswordEmailKo(request);

    assertKo(response,
             HttpStatus.NOT_FOUND,
             SEND_FORGOTTEN_PASSWORD_EMAIL,
             UserNotFoundException.ERROR_MSG + UNKNOWN_EMAIL);

    verify(notificationService, never()).sendNotification(any(ChannelEnum.class), any());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "not-an-email", "missing-at.example.com", "@example.com", "foo@"})
  void shouldReturn400_whenEmailIsInvalid(String email) {
    ForgotPasswordRequest request = new ForgotPasswordRequest(email);
    ResponseEntity<ErrorDto> response = securityClient.sendForgottenPasswordEmailKo(request);
    assertKo(response, HttpStatus.BAD_REQUEST, SEND_FORGOTTEN_PASSWORD_EMAIL);
  }
}
