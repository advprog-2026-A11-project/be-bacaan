package id.ac.ui.cs.advprog.yomu;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class YomuApplicationTests {

  @Test
  void contextLoads() {
  }

  @Test
  void runMain() {
    System.setProperty("server.port", "0"); // use available port so that it won't conflict with port 8080 (if we use port 8080 to run main program)
    YomuApplication.main(new String[] {});
  }

}
