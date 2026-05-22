package id.ac.ui.cs.advprog.yomu.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupabaseJwtFilterTest {

  private final JwtDecoder jwtDecoder = mock(JwtDecoder.class);
  private final SupabaseJwtFilter filter = new SupabaseJwtFilter(jwtDecoder);
  private final FilterChain filterChain = mock(FilterChain.class);

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternalWithoutAuthorizationHeaderShouldContinueFilterChain() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtDecoder);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternalWithNonBearerHeaderShouldReturnUnauthorized() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(HttpHeaders.AUTHORIZATION, "Basic abc123");

    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain, never()).doFilter(request, response);
    verifyNoInteractions(jwtDecoder);

    assertEquals(401, response.getStatus());
    assertEquals("application/json", response.getContentType());
    assertTrue(response.getContentAsString()
        .contains("Authorization header must use Bearer token"));
  }

  @Test
  void doFilterInternalWithEmptyBearerTokenShouldReturnUnauthorized() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer   ");

    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain, never()).doFilter(request, response);
    verifyNoInteractions(jwtDecoder);

    assertEquals(401, response.getStatus());
    assertTrue(response.getContentAsString().contains("Bearer token is empty"));
  }

  @Test
  void doFilterInternalWithValidJwtAndAppRoleShouldSetAuthentication() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");

    MockHttpServletResponse response = new MockHttpServletResponse();

    Jwt jwt = createJwt("user-123", Map.of("app_role", "admin"));
    when(jwtDecoder.decode("valid-token")).thenReturn(jwt);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    assertNotNull(authentication);
    assertTrue(authentication.isAuthenticated());
    assertInstanceOf(YomulPrincipal.class, authentication.getPrincipal());

    YomulPrincipal principal = (YomulPrincipal) authentication.getPrincipal();

    assertEquals("user-123", principal.userId());
    assertEquals("ADMIN", principal.role());
    assertTrue(authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
  }

  @Test
  void doFilterInternalWithAuthenticatedRoleShouldDefaultToStudent() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");

    MockHttpServletResponse response = new MockHttpServletResponse();

    Jwt jwt = createJwt("user-456", Map.of("role", "authenticated"));
    when(jwtDecoder.decode("valid-token")).thenReturn(jwt);

    filter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    assertNotNull(authentication);
    assertInstanceOf(YomulPrincipal.class, authentication.getPrincipal());

    YomulPrincipal principal = (YomulPrincipal) authentication.getPrincipal();

    assertEquals("user-456", principal.userId());
    assertEquals("STUDENT", principal.role());
    assertTrue(authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_STUDENT")));

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternalWithNormalRoleShouldUppercaseRole() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");

    MockHttpServletResponse response = new MockHttpServletResponse();

    Jwt jwt = createJwt("user-789", Map.of("role", "teacher"));
    when(jwtDecoder.decode("valid-token")).thenReturn(jwt);

    filter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    assertNotNull(authentication);

    YomulPrincipal principal = (YomulPrincipal) authentication.getPrincipal();

    assertEquals("user-789", principal.userId());
    assertEquals("TEACHER", principal.role());
    assertTrue(authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_TEACHER")));

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternalWithoutRoleShouldDefaultToStudent() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");

    MockHttpServletResponse response = new MockHttpServletResponse();

    Jwt jwt = createJwt("user-999", Map.of());
    when(jwtDecoder.decode("valid-token")).thenReturn(jwt);

    filter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    assertNotNull(authentication);

    YomulPrincipal principal = (YomulPrincipal) authentication.getPrincipal();

    assertEquals("user-999", principal.userId());
    assertEquals("STUDENT", principal.role());
    assertTrue(authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_STUDENT")));

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternalWithInvalidJwtShouldClearContextAndReturnUnauthorized() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");

    MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtDecoder.decode("invalid-token"))
        .thenThrow(new JwtException("expired"));

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain, never()).doFilter(request, response);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(401, response.getStatus());
    assertTrue(response.getContentAsString()
        .contains("Invalid or expired token: expired"));
  }

  private Jwt createJwt(String subject, Map<String, Object> claims) {
    Map<String, Object> allClaims = new HashMap<>();
    allClaims.put("sub", subject);
    allClaims.putAll(claims);

    return new Jwt(
        "token-value",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        Map.of("alg", "HS256"),
        allClaims
    );
  }
}