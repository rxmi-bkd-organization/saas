package org.bkd.saas.user.runner;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.bkd.saas.user.dto.RoleEnum;
import org.bkd.saas.user.dto.UserDto;
import org.bkd.saas.user.rest.request.CreateUserRequest;
import org.bkd.saas.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminSeedingRunner implements ApplicationRunner {
  private final String email;
  private final String password;
  private final Validator validator;
  private final UserService userService;

  public AdminSeedingRunner(@Value("${app.admin.email}") String email,
                            @Value("${app.admin.password}") String password,
                            Validator validator,
                            UserService userService) {
    this.email = email;
    this.password = password;
    this.validator = validator;
    this.userService = userService;
  }

  @Override
  public void run(ApplicationArguments args) {
    CreateUserRequest request = new CreateUserRequest(email, password);
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    if (violations.isEmpty()) {
      UserDto user = userService.getOrCreateUser(request.email(), request.password(), false);

      if (!user.enabled()) {
        userService.enableUser(user.id());
      }

      if (user.role() != RoleEnum.ROLE_ADMIN) {
        userService.updateUserRole(user.id(), RoleEnum.ROLE_ADMIN);
      }
    }
    else {
      String reasons = violations.stream()
                                 .map(v -> v.getPropertyPath() + " " + v.getMessage())
                                 .sorted()
                                 .collect(Collectors.joining(", "));

      log.error("Admin user seeding failed, invalid app.admin.email or app.admin.password: {}", reasons);
    }
  }
}
