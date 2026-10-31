package org.bkd.saas.security.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.bkd.saas.security.exception.InvalidCredentialsException;
import org.bkd.saas.security.exception.InvalidTokenException;
import org.bkd.saas.shared.dto.ErrorDto;
import org.bkd.saas.shared.factory.ErrorDtoFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SecurityControllerAdvice {

  @ExceptionHandler({InvalidCredentialsException.class, InvalidTokenException.class})
  public ResponseEntity<ErrorDto> handleUnauthorized(RuntimeException e, HttpServletRequest request) {
    return ErrorDtoFactory.buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage(), request);
  }
}
