package id.ac.ui.cs.advprog.yomu.config;

import id.ac.ui.cs.advprog.yomu.security.SupabaseJwtFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfig {

  @Value("${supabase.url:}")
  private String supabaseUrl;

  @Value("${supabase.jwks-url:}")
  private String configuredJwksUrl;

  @Bean
  public JwtDecoder jwtDecoder() {
    String jwksUrl = resolveJwksUrl();
    return NimbusJwtDecoder.withJwkSetUri(jwksUrl)
        .jwsAlgorithms(algorithms -> {
          algorithms.add(SignatureAlgorithm.ES256);
          algorithms.add(SignatureAlgorithm.RS256);
          algorithms.add(SignatureAlgorithm.RS512);
        })
        .build();
  }

  @Bean
  public SupabaseJwtFilter supabaseJwtFilter(JwtDecoder jwtDecoder) {
    return new SupabaseJwtFilter(jwtDecoder);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      SupabaseJwtFilter supabaseJwtFilter) throws Exception {

    http
        // Disable CSRF because we use stateless JWT authentication (no session/cookies)
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/student/**").authenticated()
            .anyRequest().permitAll()
        )
        .addFilterBefore(supabaseJwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of(
        "http://localhost:3000",
        "http://localhost:3001",
        "https://*.vercel.app"   // untuk deployment
    ));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  private String resolveJwksUrl() {
    if (StringUtils.hasText(configuredJwksUrl)) {
      return configuredJwksUrl;
    }
    if (!StringUtils.hasText(supabaseUrl)) {
      throw new IllegalStateException(
          "SUPABASE_URL atau SUPABASE_JWKS_URL harus di-set di .env.local"
      );
    }
    return trimSlash(supabaseUrl) + "/auth/v1/.well-known/jwks.json";
  }

  private String trimSlash(String url) {
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }
}