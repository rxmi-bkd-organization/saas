package org.bkd.saas.notification.channel;

import lombok.extern.slf4j.Slf4j;
import org.bkd.saas.notification.dto.ChannelEnum;
import org.bkd.saas.notification.dto.NotificationDto;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsChannel implements NotificationChannel {

  @Override
  public ChannelEnum type() {
    return ChannelEnum.SMS;
  }

  @Override
  public void sendNotification(NotificationDto notification) {
    log.info("sms to {}: {}", notification.recipient(), notification.body());
  }
}
