package org.bkd.saas.security.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bkd.saas.security.dto.AccessTokenDto;
import org.bkd.saas.security.dto.RateLimitPolicyEnum;
import org.bkd.saas.security.dto.UserDetailsDto;
import org.bkd.saas.security.exception.InvalidCredentialsException;
import org.bkd.saas.security.exception.InvalidTokenException;
import org.bkd.saas.security.publisher.SecurityEventPublisher;
import org.bkd.saas.user.dto.UserDto;
import org.bkd.saas.user.dto.UserWithPasswordDto;
import org.bkd.saas.user.exception.DisabledUserException;
import org.bkd.saas.user.exception.UserAlreadyEnabledException;
import org.bkd.saas.user.exception.UserNotFoundException;
import org.bkd.saas.user.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityService {
  private final UserService userService;
  private final EmailService emailService;
  private final AuthenticationManager authenticationManager;
  private final SecurityContextRepository securityContextRepository;
  private final PasswordResetTokenService passwordResetTokenService;
  private final ActivationTokenService activationTokenService;
  private final SecurityEventPublisher securityEventPublisher;
  private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
  private final RateLimitService rateLimitService;
  private final AccessTokenService accessTokenService;

  public UserDto login(String email, String password, HttpServletRequest request, HttpServletResponse response) {
    UserDetailsDto userDetails = authenticate(email, password);
    Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(userDetails.getId(),
                                                                                      null,
                                                                                      userDetails.getAuthorities());

    sessionAuthenticationStrategy.onAuthentication(authentication, request, response);
    saveSecurityContext(authentication, request, response);
    securityEventPublisher.publishInteractiveAuthenticationSuccessEvent(authentication);
    return userService.readUser(userDetails.getId());
  }

  public AccessTokenDto loginMobile(String email, String password) {
    UserDetailsDto userDetails = authenticate(email, password);
    String accessToken = accessTokenService.createJwt(userDetails.getId(), userDetails.getRole());
    return new AccessTokenDto(accessToken, AccessTokenDto.BEARER, accessTokenService.getExpirationInSeconds());
  }

  private UserDetailsDto authenticate(String email, String password) {
    try {
      rateLimitService.checkAvailableOrThrow(RateLimitPolicyEnum.LOGIN_FAILURES_BY_EMAIL, email);
      UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.unauthenticated(email, password);
      Authentication authenticated = authenticationManager.authenticate(token);
      return (UserDetailsDto) authenticated.getPrincipal();
    }
    catch (DisabledException | LockedException e) {
      rateLimitService.consume(RateLimitPolicyEnum.LOGIN_FAILURES_BY_EMAIL, email);
      throw new DisabledUserException(email);
    }
    catch (BadCredentialsException e) {
      rateLimitService.consume(RateLimitPolicyEnum.LOGIN_FAILURES_BY_EMAIL, email);
      throw new InvalidCredentialsException();
    }
  }

  private void saveSecurityContext(Authentication authentication,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    securityContextRepository.saveContext(securityContext, request, response);
  }

  public void enableUser(String jwt) {
    UUID userId = activationTokenService.readSubject(jwt);
    userService.enableUser(userId);
  }

  public void sendActivationEmail(String email) {
    rateLimitService.consumeOrThrow(RateLimitPolicyEnum.SEND_EMAIL_BY_RECIPIENT, email);
    UserDto user = userService.readUser(email).orElseThrow(() -> new UserNotFoundException(email));

    if (user.enabled()) {
      throw new UserAlreadyEnabledException(email);
    }

    emailService.sendActivationEmail(user.id(), user.email());
  }

  public void sendForgottenPasswordEmail(String email) {
    rateLimitService.consumeOrThrow(RateLimitPolicyEnum.SEND_EMAIL_BY_RECIPIENT, email);

    UserWithPasswordDto user = userService.readOptionalUserWithPassword(email)
                                          .orElseThrow(() -> new UserNotFoundException(email));

    String jwt = passwordResetTokenService.createJwt(user.id(), user.passwordVersion());
    emailService.sendForgottenPasswordEmail(user.email(), jwt);
  }

  public void resetPassword(String jwt, String newPassword) {
    boolean isValidJwt = passwordResetTokenService.isValidJwt(jwt);

    if (isValidJwt) {
      UUID userId = passwordResetTokenService.readSubject(jwt);
      userService.updateUserPassword(userId, newPassword);
      return;
    }

    throw new InvalidTokenException(jwt);
  }
}
