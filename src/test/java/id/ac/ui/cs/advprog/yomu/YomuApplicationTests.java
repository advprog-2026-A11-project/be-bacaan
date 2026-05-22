package id.ac.ui.cs.advprog.yomu;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(properties = "app.security.enabled=false")
class YomuApplicationTests {

  @Test
  void contextLoads() {
  }

  @Test
  void runMain() {
    System.setProperty("server.port", "0");
    System.setProperty("app.security.enabled", "false");

    assertDoesNotThrow(() ->
        YomuApplication.main(new String[]{}));
  }
}