package org.bkd.saas.user.rest;

import static org.bkd.saas.user.rest.Routes.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bkd.saas.user.dto.UserDto;
import org.bkd.saas.user.rest.request.CreateUserRequest;
import org.bkd.saas.user.rest.request.UpdateUserPasswordRequest;
import org.bkd.saas.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping(ME)
  public ResponseEntity<UserDto> me(@AuthenticationPrincipal UUID userId) {
    return ResponseEntity.ok(userService.readUser(userId));
  }

  @PostMapping(CREATE_USER)
  public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request.email(), request.password()));
  }

  @PutMapping(UPDATE_USER_PASSWORD)
  public ResponseEntity<Void> updateUserPassword(@AuthenticationPrincipal UUID userId,
                                                 @Valid @RequestBody UpdateUserPasswordRequest request,
                                                 HttpSession session) {
    userService.updateUserPassword(userId, request.oldPassword(), request.newPassword(), session.getId());
    return ResponseEntity.noContent().build();
  }

  /**
   * @PutMapping(UPDATE_USER_EMAIL)
   *                                public ResponseEntity<Void> updateUserEmail(@AuthenticationPrincipal UUID userId,
   * @Valid @RequestBody UpdateUserEmailRequest request) {
   *        userService.updateUserEmail(userId, request.email());
   *        return ResponseEntity.noContent().build();
   *        }
   **/
}
