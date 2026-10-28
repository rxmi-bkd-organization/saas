package org.bkd.saas;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@ActiveProfiles("local")
public abstract class AbstractIntegrationTest {
  @LocalServerPort
  private int port;

  @ServiceConnection
  private static final PostgreSQLContainer postgresql = new PostgreSQLContainer("postgres:17-alpine");

  private String host() {
    return "http://localhost:" + port;
  }
}
