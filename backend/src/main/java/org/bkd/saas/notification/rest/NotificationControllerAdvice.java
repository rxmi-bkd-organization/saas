package org.bkd.saas.notification.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.bkd.saas.notification.exception.UnsupportedChannelException;
import org.bkd.saas.shared.dto.ErrorDto;
import org.bkd.saas.shared.factory.ErrorDtoFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NotificationControllerAdvice {

  @ExceptionHandler(UnsupportedChannelException.class)
  public ResponseEntity<ErrorDto> handleUnsupportedChannel(UnsupportedChannelException e, HttpServletRequest request) {
    return ErrorDtoFactory.buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), request);
  }
}
