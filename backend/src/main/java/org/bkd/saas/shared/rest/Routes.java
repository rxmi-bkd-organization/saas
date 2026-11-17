package org.bkd.saas.shared.rest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Routes {

  public static final String PRIVATE_BASE_PATH = "/api/v1";
  public static final String PUBLIC_BASE_PATH = PRIVATE_BASE_PATH + "/public";

  public static final String MOBILE = "/mobile";

  public static final String PRIVATE_MOBILE_BASE_PATH = PRIVATE_BASE_PATH + MOBILE;
  public static final String PUBLIC_MOBILE_BASE_PATH = PUBLIC_BASE_PATH + MOBILE;
}
