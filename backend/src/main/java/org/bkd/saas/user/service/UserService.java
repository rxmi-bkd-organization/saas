package org.bkd.saas.user.service;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bkd.saas.user.db.AppUserEntity;
import org.bkd.saas.user.db.UserRepository;
import org.bkd.saas.user.dto.UserDto;
import org.bkd.saas.user.dto.UserWithPasswordDto;
import org.bkd.saas.user.exception.EmailAlreadyUsedException;
import org.bkd.saas.user.exception.PasswordMismatchException;
import org.bkd.saas.user.exception.UserAlreadyEnabledException;
import org.bkd.saas.user.exception.UserNotFoundException;
import org.bkd.saas.user.mapper.UserMapper;
import org.bkd.saas.user.publisher.UserEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final UserEventPublisher userEventPublisher;

  @Transactional
  public UserDto createUser(String email, String password) {
    boolean isEmailUsed = userRepository.findByEmail(email).isPresent();

    if (isEmailUsed) {
      throw new EmailAlreadyUsedException(email);
    }

    AppUserEntity user = new AppUserEntity(email);
    setPassword(user, password);
    user = userRepository.save(user);
    userEventPublisher.publishUserCreatedEvent(user.getId(), user.getEmail());
    return userMapper.toUserDto(user);
  }

  private void setPassword(AppUserEntity user, String password) {
    user.setPassword(passwordEncoder.encode(password));
    user.setPasswordVersion(user.getPasswordVersion() + 1);
  }

  public UserDto readUser(UUID userId) {
    return userRepository.findById(userId)
                         .map(userMapper::toUserDto)
                         .orElseThrow(() -> new UserNotFoundException(userId));
  }

  public Optional<UserDto> readUser(String email) {
    return userRepository.findByEmail(email).map(userMapper::toUserDto);
  }

  public Optional<UserWithPasswordDto> readOptionalUserWithPassword(String email) {
    return userRepository.findByEmail(email).map(userMapper::toUserWithPasswordDto);
  }

  public UserWithPasswordDto readUserWithPassword(UUID userId) {
    return userRepository.findById(userId)
                         .map(userMapper::toUserWithPasswordDto)
                         .orElseThrow(() -> new UserNotFoundException(userId));
  }

  @Transactional
  public void enableUser(UUID userId) {
    AppUserEntity user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

    if (user.isEnabled()) {
      throw new UserAlreadyEnabledException(userId);
    }
    user.setEnabled(true);
    userRepository.save(user);
  }

  /**
   * @Transactional
   *                public void updateUserEmail(UUID userId, String email) {
   *                String normalized = StringUtils.normalizeEmail(email);
   *                AppUserEntity user = userRepository.findById(userId).orElseThrow(() -> new
   *                UserNotFoundException(userId));
   * 
   *                boolean isEmailUsedByAnotherUser = userRepository.findByEmail(normalized)
   *                .filter(appUserEntity -> !appUserEntity.getId().equals(userId))
   *                .isPresent();
   * 
   *                if (isEmailUsedByAnotherUser) {
   *                throw new EmailAlreadyUsedException(normalized);
   *                }
   * 
   *                user.setEmail(normalized);
   *                userRepository.save(user);
   *                }
   **/

  @Transactional
  public void updateUserPassword(UUID userId, String newPassword) {
    AppUserEntity user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    setPassword(user, newPassword);
    userRepository.save(user);
    userEventPublisher.publishUserPasswordUpdatedEvent(userId);
  }

  @Transactional
  public void updateUserPassword(UUID userId, String oldPassword, String newPassword, String currentSessionId) {
    AppUserEntity user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

    boolean isOldPasswordCorrect = passwordEncoder.matches(oldPassword, user.getPassword());

    if (!isOldPasswordCorrect) {
      throw new PasswordMismatchException();
    }

    setPassword(user, newPassword);
    userRepository.save(user);
    userEventPublisher.publishUserPasswordUpdatedEvent(userId, currentSessionId);
  }
}
