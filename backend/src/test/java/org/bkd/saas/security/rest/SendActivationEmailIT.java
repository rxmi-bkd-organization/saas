package org.bkd.saas.security.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bkd.saas.security.rest.Routes.SEND_ACTIVATION_EMAIL;
import static org.bkd.saas.security.rest.SecurityAssertions.assertSendActivationEmailOk;
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
import org.bkd.saas.security.rest.request.SendActivationEmailRequest;
import org.bkd.saas.security.service.ActivationTokenService;
import org.bkd.saas.shared.dto.ErrorDto;
import org.bkd.saas.user.db.AppUserEntity;
import org.bkd.saas.user.db.UserRepository;
import org.bkd.saas.user.exception.UserAlreadyEnabledException;
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

class SendActivationEmailIT extends AbstractIntegrationTest {
  private static final String EMAIL = "send-activation@example.com";
  private static final String UNNORMALIZED_EMAIL = "  Send-Activation@Example.COM ";
  private static final String UNKNOWN_EMAIL = "unknown@example.com";
  private static final String PASSWORD = "password123";
  private static final String ACTIVATION_SUBJECT = "Activate your account";
  private static final String TOKEN_PARAM = "/enable-account?token=";

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ActivationTokenService activationTokenService;

  @MockitoSpyBean
  private NotificationService notificationService;

  @BeforeEach
  void beforeEach() {
    CreateUserRequest request = new CreateUserRequest(EMAIL, PASSWORD);
    userClient.createUserOk(request);
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

  private NotificationDto assertThatEmailWasSent() {
    ArgumentCaptor<NotificationDto> captor = ArgumentCaptor.forClass(NotificationDto.class);
    verify(notificationService).sendNotification(eq(ChannelEnum.EMAIL), captor.capture());
    return captor.getValue();
  }

  @Test
  void shouldReturn204_whenUserIsDisabled() {
    SendActivationEmailRequest request = new SendActivationEmailRequest(EMAIL);
    ResponseEntity<Void> response = securityClient.sendActivationEmailOk(request);
    assertSendActivationEmailOk(response);
  }

  @Test
  void shouldSendActivationEmail_whenUserIsDisabled() {
    SendActivationEmailRequest request = new SendActivationEmailRequest(EMAIL);
    securityClient.sendActivationEmailOk(request);
    assertThatEmailWasSent();
  }

  @Test
  void shouldSendEmailWithValidToken_whenUserIsDisabled() {
    SendActivationEmailRequest request = new SendActivationEmailRequest(EMAIL);
    securityClient.sendActivationEmailOk(request);
    NotificationDto notification = assertThatEmailWasSent();
    String body = notification.body();
    String jwt = body.substring(body.indexOf(TOKEN_PARAM) + TOKEN_PARAM.length());
    boolean isValidJwt = activationTokenService.isValidJwt(jwt);
    assertThat(isValidJwt).isTrue();
  }

  @Test
  void shouldNormalizeEmail_whenEmailHasCaseAndWhitespace() {
    SendActivationEmailRequest request = new SendActivationEmailRequest(UNNORMALIZED_EMAIL);
    ResponseEntity<Void> response = securityClient.sendActivationEmailOk(request);
    assertSendActivationEmailOk(response);
    NotificationDto notification = assertThatEmailWasSent();
    assertThat(notification.recipient()).isEqualTo(EMAIL);
  }

  @Test
  void shouldReturn404AndSendNothing_whenEmailIsUnknown() {
    SendActivationEmailRequest request = new SendActivationEmailRequest(UNKNOWN_EMAIL);
    ResponseEntity<ErrorDto> response = securityClient.sendActivationEmailKo(request);
    assertKo(response, HttpStatus.NOT_FOUND, SEND_ACTIVATION_EMAIL, UserNotFoundException.ERROR_MSG + UNKNOWN_EMAIL);
    verify(notificationService, never()).sendNotification(any(ChannelEnum.class), any());
  }

  @Test
  void shouldReturn409AndSendNothing_whenUserIsAlreadyEnabled() {
    enableUser();
    SendActivationEmailRequest request = new SendActivationEmailRequest(EMAIL);
    ResponseEntity<ErrorDto> response = securityClient.sendActivationEmailKo(request);
    assertKo(response, HttpStatus.CONFLICT, SEND_ACTIVATION_EMAIL, UserAlreadyEnabledException.ERROR_MSG + EMAIL);
    verify(notificationService, never()).sendNotification(any(ChannelEnum.class), any());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "not-an-email", "missing-at.example.com", "@example.com", "foo@"})
  void shouldReturn400_whenEmailIsInvalid(String email) {
    SendActivationEmailRequest request = new SendActivationEmailRequest(email);
    ResponseEntity<ErrorDto> response = securityClient.sendActivationEmailKo(request);
    assertKo(response, HttpStatus.BAD_REQUEST, SEND_ACTIVATION_EMAIL);
  }
}
