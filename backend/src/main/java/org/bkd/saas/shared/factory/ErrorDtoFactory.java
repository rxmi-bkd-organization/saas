package org.bkd.saas.shared.factory;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bkd.saas.shared.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorDtoFactory {

  public static ErrorDto buildError(HttpStatus status, String message, String path) {
    return new ErrorDto(Instant.now(), status.value(), status.getReasonPhrase(), message, path);
  }

  public static ResponseEntity<ErrorDto> buildErrorResponse(HttpStatus status,
                                                            String message,
                                                            HttpServletRequest request) {
    return ResponseEntity.status(status).body(buildError(status, message, request.getRequestURI()));
  }
}
