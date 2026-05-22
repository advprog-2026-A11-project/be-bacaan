package id.ac.ui.cs.advprog.yomu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    // Supabase menyimpan role di klaim "role"
    grantedAuthoritiesConverter.setAuthoritiesClaimName("role");
    grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
      String appRole = jwt.getClaimAsString("user_role");
      String supabaseRole = jwt.getClaimAsString("role");

      String resolvedRole;
      if (StringUtils.hasText(appRole)) {
        resolvedRole = appRole.toUpperCase();
      } else if ("authenticated".equals(supabaseRole)) {
        resolvedRole = "STUDENT";
      } else {
        resolvedRole = StringUtils.hasText(supabaseRole) ? supabaseRole.toUpperCase() : "STUDENT";
      }

      return List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + resolvedRole));
    });

    return jwtAuthenticationConverter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // Endpoint publik: daftar semua bacaan boleh diakses tanpa login
            .requestMatchers(HttpMethod.GET, "/api/student/readings").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/student/readings/").permitAll()

            // Endpoint admin: hanya role ADMIN
            .requestMatchers("/api/admin/**").hasRole("ADMIN")

            // Semua endpoint student lainnya: wajib login (token valid)
            .requestMatchers("/api/student/**").authenticated()

            // Endpoint lain: tolak
            .anyRequest().authenticated()
        )
        // Aktifkan OAuth2 Resource Server dengan JWT
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
        );

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of(
        "http://localhost:3000",
        "http://localhost:3001",
        "https://*.vercel.app",
        "https://*.up.railway.app"
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