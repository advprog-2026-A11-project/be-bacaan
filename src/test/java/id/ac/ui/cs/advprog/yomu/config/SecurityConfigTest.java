package id.ac.ui.cs.advprog.yomu.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

  @Test
  void jwtAuthenticationConverterShouldUseUserRoleWhenPresent() {
    Authentication authentication = new SecurityConfig()
        .jwtAuthenticationConverter()
        .convert(jwtWithClaims("user_role", "admin"));

    assertTrue(authentication.getAuthorities().stream()
        .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
  }

  @Test
  void jwtAuthenticationConverterShouldMapAuthenticatedSupabaseRoleToStudent() {
    Authentication authentication = new SecurityConfig()
        .jwtAuthenticationConverter()
        .convert(jwtWithClaims("role", "authenticated"));

    assertTrue(authentication.getAuthorities().stream()
        .anyMatch(authority -> "ROLE_STUDENT".equals(authority.getAuthority())));
  }

  @Test
  void jwtAuthenticationConverterShouldUseSupabaseRoleWhenSpecificRolePresent() {
    Authentication authentication = new SecurityConfig()
        .jwtAuthenticationConverter()
        .convert(jwtWithClaims("role", "mentor"));

    assertTrue(authentication.getAuthorities().stream()
        .anyMatch(authority -> "ROLE_MENTOR".equals(authority.getAuthority())));
  }

  @Test
  void jwtAuthenticationConverterShouldDefaultToStudentWhenNoRolePresent() {
    Authentication authentication = new SecurityConfig()
        .jwtAuthenticationConverter()
        .convert(jwtWithClaims("sub", "user-123"));

    assertTrue(authentication.getAuthorities().stream()
        .anyMatch(authority -> "ROLE_STUDENT".equals(authority.getAuthority())));
  }

  @Test
  void jwtDecoderShouldBeCreatedWhenConfiguredJwksUrlExists() {
    SecurityConfig config = new SecurityConfig();
    ReflectionTestUtils.setField(config, "configuredJwksUrl",
        "http://localhost/.well-known/jwks.json");

    assertNotNull(config.jwtDecoder());
  }

  @Test
  void resolveJwksUrlShouldPreferConfiguredUrl() {
    SecurityConfig config = new SecurityConfig();
    ReflectionTestUtils.setField(config, "configuredJwksUrl", "http://configured/jwks");
    ReflectionTestUtils.setField(config, "supabaseUrl", "http://supabase");

    String result = ReflectionTestUtils.invokeMethod(config, "resolveJwksUrl");

    assertEquals("http://configured/jwks", result);
  }

  @Test
  void resolveJwksUrlShouldBuildFromSupabaseUrlWithoutTrailingSlash() {
    SecurityConfig config = new SecurityConfig();
    ReflectionTestUtils.setField(config, "configuredJwksUrl", "");
    ReflectionTestUtils.setField(config, "supabaseUrl", "http://supabase");

    String result = ReflectionTestUtils.invokeMethod(config, "resolveJwksUrl");

    assertEquals("http://supabase/auth/v1/.well-known/jwks.json", result);
  }

  @Test
  void resolveJwksUrlShouldBuildFromSupabaseUrlWithTrailingSlash() {
    SecurityConfig config = new SecurityConfig();
    ReflectionTestUtils.setField(config, "configuredJwksUrl", "");
    ReflectionTestUtils.setField(config, "supabaseUrl", "http://supabase/");

    String result = ReflectionTestUtils.invokeMethod(config, "resolveJwksUrl");

    assertEquals("http://supabase/auth/v1/.well-known/jwks.json", result);
  }

  @Test
  void resolveJwksUrlShouldThrowWhenNoUrlConfigured() {
    SecurityConfig config = new SecurityConfig();
    ReflectionTestUtils.setField(config, "configuredJwksUrl", "");
    ReflectionTestUtils.setField(config, "supabaseUrl", "");

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> ReflectionTestUtils.invokeMethod(config, "resolveJwksUrl"));

    assertEquals("SUPABASE_URL atau SUPABASE_JWKS_URL harus di-set di .env.local",
        exception.getMessage());
  }

  private Jwt jwtWithClaims(String claimName, String claimValue) {
    return Jwt.withTokenValue("token")
        .header("alg", "none")
        .claim(claimName, claimValue)
        .build();
  }
}
