package org.bkd.saas.security.configuration;

import static org.bkd.saas.security.rest.Routes.LOGOUT_ENDPOINT;
import static org.bkd.saas.shared.rest.Routes.PRIVATE_MOBILE_BASE_PATH;
import static org.bkd.saas.shared.rest.Routes.PUBLIC_BASE_PATH;
import static org.bkd.saas.shared.rest.Routes.PUBLIC_MOBILE_BASE_PATH;
import static org.bkd.saas.user.exception.UserNotFoundException.ERROR_MSG;

import org.bkd.saas.security.filter.AccessTokenFilter;
import org.bkd.saas.security.filter.RateLimitFilter;
import org.bkd.saas.security.mapper.UserDetailsMapper;
import org.bkd.saas.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.SecurityContextConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Bean
  @Order(1)
  public SecurityFilterChain mobileSecurityFilterChain(HttpSecurity http,
                                                       AccessTokenFilter accessTokenFilter,
                                                       AuthenticationEntryPoint authenticationEntryPoint,
                                                       AccessDeniedHandler accessDeniedHandler,
                                                       RateLimitFilter rateLimitFilter) {

    return http.securityMatcher(PUBLIC_MOBILE_BASE_PATH + "/**", PRIVATE_MOBILE_BASE_PATH + "/**")
               .csrf(csrfConfigurer(false))
               .logout(logoutConfigurer(null, null, false))
               .sessionManagement(sessionCreationPolicyConfigurer(SessionCreationPolicy.STATELESS))
               .exceptionHandling(exceptionHandlingConfigurer(authenticationEntryPoint, accessDeniedHandler))
               .authorizeHttpRequests(mobileAuthorizeHttpRequestsConfigurer())
               .addFilterBefore(rateLimitFilter, AnonymousAuthenticationFilter.class)
               .addFilterBefore(accessTokenFilter, AnonymousAuthenticationFilter.class)
               .build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain webSecurityFilterChain(HttpSecurity http,
                                                    @Value("${app.security.csrf.enabled:true}") boolean csrfEnabled,
                                                    AuthenticationManager authenticationManager,
                                                    SessionAuthenticationStrategy sessionAuthenticationStrategy,
                                                    SecurityContextRepository securityContextRepository,
                                                    AuthenticationEntryPoint authenticationEntryPoint,
                                                    AccessDeniedHandler accessDeniedHandler,
                                                    LogoutSuccessHandler logoutSuccessHandler,
                                                    UserDetailsService userDetailsService,
                                                    RateLimitFilter rateLimitFilter) {

    return http.csrf(csrfConfigurer(csrfEnabled))
               .logout(logoutConfigurer(LOGOUT_ENDPOINT, logoutSuccessHandler, true))
               .authenticationManager(authenticationManager)
               .userDetailsService(userDetailsService)
               .sessionManagement(sessionManagementConfigurer(sessionAuthenticationStrategy))
               .securityContext(securityContextConfigurer(securityContextRepository))
               .exceptionHandling(exceptionHandlingConfigurer(authenticationEntryPoint, accessDeniedHandler))
               .authorizeHttpRequests(webAuthorizeHttpRequestsConfigurer())
               // so it runs early in the security chain, before the authentication and authorization steps. That makes throttled requests cheap, because they never reach a BCrypt check or a database lookup.
               .addFilterBefore(rateLimitFilter, LogoutFilter.class)
               .build();
  }

  private static Customizer<CsrfConfigurer<HttpSecurity>> csrfConfigurer(boolean enabled) {
    return enabled ? CsrfConfigurer::spa : CsrfConfigurer::disable;
  }

  private Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> webAuthorizeHttpRequestsConfigurer() {
    return authorizeHttpRequests -> authorizeHttpRequests.requestMatchers(PUBLIC_BASE_PATH + "/**",
                                                                          "/error",
                                                                          "/actuator/health/**")
                                                         .permitAll()
                                                         .anyRequest()
                                                         .authenticated();
  }

  private Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> mobileAuthorizeHttpRequestsConfigurer() {
    return authorizeHttpRequests -> authorizeHttpRequests.requestMatchers(PUBLIC_MOBILE_BASE_PATH + "/**")
                                                         .permitAll()
                                                         .anyRequest()
                                                         .authenticated();
  }

  private Customizer<LogoutConfigurer<HttpSecurity>> logoutConfigurer(String logoutUrl,
                                                                      LogoutSuccessHandler logoutSuccessHandler,
                                                                      boolean enabled) {
    if (enabled) {
      return logout -> logout.logoutUrl(logoutUrl).logoutSuccessHandler(logoutSuccessHandler);
    }
    return AbstractHttpConfigurer::disable;
  }

  private Customizer<SessionManagementConfigurer<HttpSecurity>> sessionManagementConfigurer(SessionAuthenticationStrategy sessionAuthenticationStrategy) {
    return sessionManagement -> sessionManagement.sessionAuthenticationStrategy(sessionAuthenticationStrategy);
  }

  private Customizer<SessionManagementConfigurer<HttpSecurity>> sessionCreationPolicyConfigurer(SessionCreationPolicy sessionCreationPolicy) {
    return sessionManagement -> sessionManagement.sessionCreationPolicy(sessionCreationPolicy);
  }

  private Customizer<SecurityContextConfigurer<HttpSecurity>> securityContextConfigurer(SecurityContextRepository securityContextRepository) {
    return securityContext -> securityContext.securityContextRepository(securityContextRepository);
  }

  private Customizer<ExceptionHandlingConfigurer<HttpSecurity>> exceptionHandlingConfigurer(AuthenticationEntryPoint authenticationEntryPoint,
                                                                                            AccessDeniedHandler accessDeniedHandler) {
    return exceptionHandling -> exceptionHandling.authenticationEntryPoint(authenticationEntryPoint)
                                                 .accessDeniedHandler(accessDeniedHandler);
  }

  @Bean
  public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                     PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(authenticationProvider);
  }

  @Bean
  public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    return new SessionFixationProtectionStrategy();
  }

  @Bean
  public SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint() {
    return (request, response, exception) -> response.sendError(HttpStatus.UNAUTHORIZED.value(),
                                                                "Authentication required. Please log in.");
  }

  @Bean
  public AccessDeniedHandler accessDeniedHandler() {
    return (request, response, exception) -> response.sendError(HttpStatus.FORBIDDEN.value(),
                                                                "You don't have access to this resource.");

  }

  @Bean
  public LogoutSuccessHandler logoutSuccessHandler() {
    return new HttpStatusReturningLogoutSuccessHandler();
  }

  @Bean
  public UserDetailsService userDetailsService(UserService userService, UserDetailsMapper userDetailsMapper) {
    return username -> userService.readOptionalUserWithPassword(username)
                                  .map(userDetailsMapper::toUserDetailsDto)
                                  .orElseThrow(() -> new UsernameNotFoundException(ERROR_MSG + username));
  }
}
