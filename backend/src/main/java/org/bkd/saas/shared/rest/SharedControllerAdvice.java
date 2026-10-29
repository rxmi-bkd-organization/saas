package org.bkd.saas.shared.rest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.bkd.saas.shared.dto.ErrorDto;
import org.bkd.saas.shared.factory.ErrorDtoFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class SharedControllerAdvice extends ResponseEntityExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDto> handleUnexpected(Exception e, HttpServletRequest request) throws Exception {
    // Let Spring Security's filter chain turn these into 401/403
    if (e instanceof AccessDeniedException || e instanceof AuthenticationException) {
      throw e;
    }

    log.error("Unexpected error on {}", request.getRequestURI(), e);
    return ErrorDtoFactory.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request);
  }
}
