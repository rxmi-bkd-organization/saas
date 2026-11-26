package org.bkd.saas.notification.channel;

import lombok.RequiredArgsConstructor;
import org.bkd.saas.notification.dto.ChannelEnum;
import org.bkd.saas.notification.dto.NotificationDto;
import org.bkd.saas.notification.service.BrevoService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailChannel implements NotificationChannel {

  private final BrevoService brevoService;

  @Override
  public ChannelEnum type() {
    return ChannelEnum.EMAIL;
  }

  @Override
  public void sendNotification(NotificationDto notification) {
    brevoService.sendEmail(notification.recipient(), notification.subject(), notification.body());
  }
}
