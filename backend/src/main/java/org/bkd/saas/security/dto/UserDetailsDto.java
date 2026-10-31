package org.bkd.saas.security.dto;

import java.util.Collection;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bkd.saas.user.dto.RoleEnum;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@AllArgsConstructor
public class UserDetailsDto implements UserDetails {

  @Getter
  private final UUID id;
  private final String email;
  private final RoleEnum role;
  private final boolean enabled;
  private final String password;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return role.getAuthorities();
  }

  @Override
  public @Nullable String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return enabled;
  }

  @Override
  public boolean isAccountNonLocked() {
    return enabled;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return enabled;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
