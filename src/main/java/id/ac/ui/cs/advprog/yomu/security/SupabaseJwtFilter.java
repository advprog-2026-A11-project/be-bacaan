package id.ac.ui.cs.advprog.yomu.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class SupabaseJwtFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtDecoder jwtDecoder;

  public SupabaseJwtFilter(JwtDecoder jwtDecoder) {
    this.jwtDecoder = jwtDecoder;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    // Tidak ada header → lanjutkan
    if (!StringUtils.hasText(authHeader)) {
      filterChain.doFilter(request, response);
      return;
    }

    if (!authHeader.startsWith(BEARER_PREFIX)) {
      writeUnauthorized(response, "Authorization header must use Bearer token");
      return;
    }

    String token = authHeader.substring(BEARER_PREFIX.length()).trim();
    if (!StringUtils.hasText(token)) {
      writeUnauthorized(response, "Bearer token is empty");
      return;
    }

    try {
      Jwt jwt = jwtDecoder.decode(token);

      String sub = jwt.getSubject();
      String role = resolveRole(jwt);

      YomulPrincipal principal = new YomulPrincipal(sub, role);
      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
          principal, null,
          List.of(new SimpleGrantedAuthority("ROLE_" + role))
      );
      SecurityContextHolder.getContext().setAuthentication(auth);

      filterChain.doFilter(request, response);

    } catch (JwtException ex) {
      SecurityContextHolder.clearContext();
      writeUnauthorized(response, "Invalid or expired token: " + ex.getMessage());
    }
  }

  private String resolveRole(Jwt jwt) {
    String appRole = jwt.getClaimAsString("app_role");
    if (StringUtils.hasText(appRole)) {
      return appRole.toUpperCase();
    }
    String role = jwt.getClaimAsString("role");
    if ("authenticated".equals(role)) {
      return "STUDENT"; // default role untuk user biasa
    }
    return StringUtils.hasText(role) ? role.toUpperCase() : "STUDENT";
  }

  private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(
        "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}"
    );
  }
}