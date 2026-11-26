package org.bkd.saas.security.filter;

import static org.bkd.saas.shared.rest.Routes.PRIVATE_MOBILE_BASE_PATH;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bkd.saas.security.exception.InvalidTokenException;
import org.bkd.saas.security.service.AccessTokenService;
import org.bkd.saas.user.dto.RoleEnum;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class AccessTokenFilter extends OncePerRequestFilter {
  private static final String BEARER_PREFIX = "Bearer ";

  private final AccessTokenService accessTokenService;

  @Override
  public boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getServletPath().startsWith(PRIVATE_MOBILE_BASE_PATH);
  }

  @Override
  public void doFilterInternal(HttpServletRequest request,
                               HttpServletResponse response,
                               FilterChain filterChain) throws ServletException, IOException {

    String header = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String jwt = extractJwtFromAuthorizationHeader(header);
      Authentication authentication = authentication(jwt);
      setContext(authentication);
    }
    catch (InvalidTokenException e) {
      response.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
      return;
    }

    filterChain.doFilter(request, response);
  }

  private String extractJwtFromAuthorizationHeader(String authorizationHeader) {
    return authorizationHeader.substring(BEARER_PREFIX.length());
  }

  private void setContext(Authentication authentication) {
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
  }

  private Authentication authentication(String jwt) {
    try {
      UUID userId = accessTokenService.readSubject(jwt);
      RoleEnum role = accessTokenService.readRole(jwt);
      return UsernamePasswordAuthenticationToken.authenticated(userId, null, role.getAuthorities());
    }
    catch (IllegalArgumentException | NullPointerException e) {
      throw new InvalidTokenException(jwt);
    }
  }
}
