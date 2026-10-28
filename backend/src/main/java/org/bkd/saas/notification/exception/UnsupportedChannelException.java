package org.bkd.saas.notification.exception;

import org.bkd.saas.notification.dto.ChannelEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsupportedChannelException extends RuntimeException {
  public static final String ERROR_MSG = "Unsupported notification channel: ";

  public UnsupportedChannelException(ChannelEnum channel) {
    super(ERROR_MSG + channel);
  }
}
