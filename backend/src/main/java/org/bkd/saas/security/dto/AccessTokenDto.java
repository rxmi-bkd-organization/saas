package org.bkd.saas.security.dto;

public record AccessTokenDto(String accessToken, String tokenType, long expiresIn) {
  public static final String BEARER = "Bearer";
}
