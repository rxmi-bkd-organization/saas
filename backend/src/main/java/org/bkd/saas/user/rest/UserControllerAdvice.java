package org.bkd.saas.user.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.bkd.saas.shared.dto.ErrorDto;
import org.bkd.saas.shared.factory.ErrorDtoFactory;
import org.bkd.saas.user.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserControllerAdvice {

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorDto> handleUserNotFound(UserNotFoundException e, HttpServletRequest request) {
    return ErrorDtoFactory.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage(), request);
  }

  @ExceptionHandler({EmailAlreadyUsedException.class, UserAlreadyEnabledException.class})
  public ResponseEntity<ErrorDto> handleConflict(RuntimeException e, HttpServletRequest request) {
    return ErrorDtoFactory.buildErrorResponse(HttpStatus.CONFLICT, e.getMessage(), request);
  }

  @ExceptionHandler(DisabledUserException.class)
  public ResponseEntity<ErrorDto> handleDisabledUser(DisabledUserException e, HttpServletRequest request) {
    return ErrorDtoFactory.buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), request);
  }

  @ExceptionHandler(PasswordMismatchException.class)
  public ResponseEntity<ErrorDto> handlePasswordMismatch(PasswordMismatchException e, HttpServletRequest request) {
    return ErrorDtoFactory.buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), request);
  }
}
