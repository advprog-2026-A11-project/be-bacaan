package id.ac.ui.cs.advprog.yomu.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YomulPrincipalTest {

  @Test
  void isAdminShouldReturnTrueForAdminRole() {
    YomulPrincipal principal = new YomulPrincipal("user-1", "ADMIN");

    assertTrue(principal.isAdmin());
  }

  @Test
  void isAdminShouldReturnTrueForLowercaseAdminRole() {
    YomulPrincipal principal = new YomulPrincipal("user-1", "admin");

    assertTrue(principal.isAdmin());
  }

  @Test
  void isAdminShouldReturnFalseForNonAdminRole() {
    YomulPrincipal principal = new YomulPrincipal("user-1", "STUDENT");

    assertFalse(principal.isAdmin());
  }
}