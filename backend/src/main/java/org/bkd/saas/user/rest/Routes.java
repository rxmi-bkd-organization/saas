package org.bkd.saas.user.rest;

import static org.bkd.saas.shared.rest.Routes.*;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Routes {

  public static final String USERS = "/users";

  public static final String PRIVATE_ENDPOINT = PRIVATE_BASE_PATH + USERS;
  public static final String PUBLIC_ENDPOINT = PUBLIC_BASE_PATH + USERS;

  public static final String PRIVATE_MOBILE_ENDPOINT = PRIVATE_MOBILE_BASE_PATH + USERS;

  public static final String ME = "/me";
  public static final String ME_ENDPOINT = PRIVATE_ENDPOINT + ME;
  public static final String MOBILE_ME_ENDPOINT = PRIVATE_MOBILE_ENDPOINT + ME;

  public static final String CREATE_USER_ENDPOINT = PUBLIC_ENDPOINT;

  public static final String PASSWORD = "/password";
  public static final String UPDATE_USER_PASSWORD_ENDPOINT = PRIVATE_ENDPOINT + PASSWORD;
  public static final String MOBILE_UPDATE_USER_PASSWORD_ENDPOINT = PRIVATE_MOBILE_ENDPOINT + PASSWORD;

  // public static final String EMAIL = "/email";
  // public static final String UPDATE_USER_EMAIL_ENDPOINT = PRIVATE_ENDPOINT + EMAIL;
  // public static final String MOBILE_UPDATE_USER_EMAIL_ENDPOINT = PRIVATE_MOBILE_ENDPOINT + EMAIL;
}
