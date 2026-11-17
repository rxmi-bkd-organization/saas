package org.bkd.saas.notification.exception;

public class EmailSendException extends RuntimeException {
  public static final String ERROR_MSG = "Failed to send email through Brevo";

  public EmailSendException(Throwable cause) {
    super(ERROR_MSG, cause);
  }
}
