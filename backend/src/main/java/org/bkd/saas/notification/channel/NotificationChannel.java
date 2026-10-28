package org.bkd.saas.notification.channel;

import org.bkd.saas.notification.dto.ChannelEnum;
import org.bkd.saas.notification.dto.NotificationDto;

public interface NotificationChannel {

  ChannelEnum type();

  void sendNotification(NotificationDto notification);
}
