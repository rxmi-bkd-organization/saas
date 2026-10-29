package org.bkd.saas.notification.exception;

import org.bkd.saas.notification.dto.ChannelEnum;

public class UnsupportedChannelException extends RuntimeException {
  public static final String ERROR_MSG = "Unsupported notification channel: ";

  public UnsupportedChannelException(ChannelEnum channel) {
    super(ERROR_MSG + channel);
  }
}
