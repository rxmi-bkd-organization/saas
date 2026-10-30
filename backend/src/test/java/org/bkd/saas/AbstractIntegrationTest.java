package org.bkd.saas;

import org.bkd.saas.security.SecurityClient;
import org.bkd.saas.user.UserClient;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@ActiveProfiles("local")
public abstract class AbstractIntegrationTest {
  @LocalServerPort
  private int port;

  @ServiceConnection
  private static final PostgreSQLContainer postgresql = new PostgreSQLContainer("postgres:17-alpine");

  protected String host() {
    return "http://localhost:" + port;
  }

  protected RestClient restClient;
  protected UserClient userClient;
  protected SecurityClient securityClient;

  @BeforeEach
  void initRestClients() {
    restClient = RestClient.builder().baseUrl(host()).defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {
    }).build();

    userClient = new UserClient(restClient);
    securityClient = new SecurityClient(restClient);
  }
}
