package id.ac.ui.cs.advprog.yomu.security;

public record YomulPrincipal(String userId, String role) {

  public boolean isAdmin() {
    return "ADMIN".equalsIgnoreCase(role);
  }
}