package org.bkd.saas.user.dto;

import static java.util.Collections.singletonList;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum RoleEnum {
  ROLE_USER,
  ROLE_ADMIN;

  public List<GrantedAuthority> getAuthorities() {
    GrantedAuthority authority = new SimpleGrantedAuthority(this.name());
    return singletonList(authority);
  }
}
