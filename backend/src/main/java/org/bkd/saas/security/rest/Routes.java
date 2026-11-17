package org.bkd.saas.security.rest;

import static org.bkd.saas.shared.rest.Routes.PUBLIC_BASE_PATH;
import static org.bkd.saas.shared.rest.Routes.PUBLIC_MOBILE_BASE_PATH;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Routes {

  public static final String SECURITY = "/security";

  public static final String PUBLIC_ENDPOINT = PUBLIC_BASE_PATH + SECURITY;

  public static final String PUBLIC_MOBILE_ENDPOINT = PUBLIC_MOBILE_BASE_PATH + SECURITY;

  public static final String LOGIN = "/login";
  public static final String LOGIN_ENDPOINT = PUBLIC_ENDPOINT + LOGIN;
  public static final String MOBILE_LOGIN_ENDPOINT = PUBLIC_MOBILE_ENDPOINT + LOGIN;

  public static final String LOGOUT = "/logout";
  public static final String LOGOUT_ENDPOINT = PUBLIC_ENDPOINT + LOGOUT;

  public static final String CSRF = "/csrf";
  public static final String CSRF_ENDPOINT = PUBLIC_ENDPOINT + CSRF;

  public static final String SEND_FORGOTTEN_PASSWORD_EMAIL = "/send-forgotten-password-email";
  public static final String SEND_FORGOTTEN_PASSWORD_EMAIL_ENDPOINT = PUBLIC_ENDPOINT + SEND_FORGOTTEN_PASSWORD_EMAIL;

  public static final String SEND_ACTIVATION_EMAIL = "/send-activation-email";
  public static final String SEND_ACTIVATION_EMAIL_ENDPOINT = PUBLIC_ENDPOINT + SEND_ACTIVATION_EMAIL;

  public static final String ENABLE_USER = "/enable-user";
  public static final String ENABLE_USER_ENDPOINT = PUBLIC_ENDPOINT + ENABLE_USER;

  public static final String RESET_PASSWORD = "/reset-password";
  public static final String RESET_PASSWORD_ENDPOINT = PUBLIC_ENDPOINT + RESET_PASSWORD;
}
