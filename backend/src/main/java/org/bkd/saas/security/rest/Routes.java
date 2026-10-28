package org.bkd.saas.security.rest;

import static org.bkd.saas.shared.rest.Routes.PUBLIC_BASE_PATH;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Routes {

  public static final String PUBLIC_ENDPOINT = PUBLIC_BASE_PATH + "/security";
  public static final String LOGIN = PUBLIC_ENDPOINT + "/login";
  public static final String LOGOUT = PUBLIC_ENDPOINT + "/logout";
  public static final String SEND_FORGOTTEN_PASSWORD_EMAIL = PUBLIC_ENDPOINT + "/send-forgotten-password-email";
  public static final String SEND_ACTIVATION_EMAIL = PUBLIC_ENDPOINT + "/send-activation-email";
  public static final String ENABLE_USER = PUBLIC_BASE_PATH + "/enable-user";
  public static final String RESET_PASSWORD = PUBLIC_ENDPOINT + "/reset-password";
}
