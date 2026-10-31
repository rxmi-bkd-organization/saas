package org.bkd.saas.user.db;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bkd.saas.user.dto.RoleEnum;

@Entity(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
public class AppUserEntity {

  @Id
  @Column(nullable = false, updatable = false, unique = true)
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, updatable = true, unique = true)
  private String email;

  @Column(nullable = false, updatable = true, unique = false)
  private String password;

  @Column(nullable = false, updatable = true, unique = false)
  private int passwordVersion = 0;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, updatable = true, unique = false)
  private RoleEnum role = RoleEnum.ROLE_USER;

  @Column(nullable = false, updatable = false, unique = false)
  private Instant createdAt = Instant.now();

  @Column(nullable = false, updatable = true, unique = false)
  private boolean enabled = false;

  public AppUserEntity(String email) {
    this.email = email;
  }
}
