package org.bkd.saas.security.service;

import org.bkd.saas.notification.dto.ChannelEnum;
import org.bkd.saas.notification.dto.NotificationDto;
import org.bkd.saas.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Service
public class EmailService {
  private static final String ENABLE_ACCOUNT_PATH = "/enable-account";
  private static final String RESET_PASSWORD_PATH = "/reset-password";
  private static final String TOKEN_QUERY_PARAM = "token";

  private final ActivationTokenService activationTokenService;
  private final NotificationService notificationService;
  private final String frontendBaseUrl;

  public EmailService(ActivationTokenService activationTokenService,
                      NotificationService notificationService,
                      @Value("${app.frontend.base-url}") String frontendBaseUrl) {
    this.activationTokenService = activationTokenService;
    this.notificationService = notificationService;
    this.frontendBaseUrl = frontendBaseUrl;
  }

  public void sendActivationEmail(UUID userId, String userEmail) {
    String jwt = activationTokenService.createJwt(userId);
    String url = buildFrontendUrl(ENABLE_ACCOUNT_PATH, jwt);

    notificationService.sendNotification(ChannelEnum.EMAIL,
                                         new NotificationDto(userEmail,
                                                             "Activate your account",
                                                             "Click this link to activate your account: " + url));
  }

  public void sendForgottenPasswordEmail(String userEmail, String jwt) {
    String url = buildFrontendUrl(RESET_PASSWORD_PATH, jwt);

    notificationService.sendNotification(ChannelEnum.EMAIL,
                                         new NotificationDto(userEmail,
                                                             "Forgot your password ?",
                                                             "Click this link to reset your password: " + url));
  }

  private String buildFrontendUrl(String path, String jwt) {
    return UriComponentsBuilder.fromUriString(frontendBaseUrl)
                               .path(path)
                               .queryParam(TOKEN_QUERY_PARAM, jwt)
                               .build()
                               .toUriString();
  }
}
