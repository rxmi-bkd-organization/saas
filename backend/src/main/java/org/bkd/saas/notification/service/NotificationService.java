package org.bkd.saas.notification.service;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.bkd.saas.notification.channel.NotificationChannel;
import org.bkd.saas.notification.dto.ChannelEnum;
import org.bkd.saas.notification.dto.NotificationDto;
import org.bkd.saas.notification.exception.UnsupportedChannelException;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

  private final Map<ChannelEnum, NotificationChannel> channels = new EnumMap<>(ChannelEnum.class);

  public NotificationService(List<NotificationChannel> notificationChannels) {
    notificationChannels.forEach(channel -> channels.put(channel.type(), channel));
  }

  public void sendNotification(ChannelEnum type, NotificationDto notification) {
    NotificationChannel channel = channels.get(type);

    if (channel == null) {
      throw new UnsupportedChannelException(type);
    }

    channel.sendNotification(notification);
  }

  public void sendNotification(Collection<ChannelEnum> types, NotificationDto notification) {
    types.forEach(type -> sendNotification(type, notification));
  }
}
