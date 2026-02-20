package id.ac.ui.cs.advprog.yomu.entity;

import id.ac.ui.cs.advprog.yomu.entity.TextContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TextContextTest {

  @Test
  void testNoArgsConstructor() {
    TextContext text = new TextContext();
    text.setId("1");
    text.setTitle("Malin Kundang");
    text.setContent("Malin kundang adalah anak yang durhaka");
    text.setCategory("Cerita rakyat");
    text.setDifficultyLevel("Low");

    assertEquals("1", text.getId());
    assertEquals("Malin Kundang", text.getTitle());
    assertEquals("Malin kundang adalah anak yang durhaka", text.getContent());
    assertEquals("Cerita rakyat", text.getCategory());
    assertEquals("Low", text.getDifficultyLevel());
  }

  @Test
  void testAllArgsContructor() {
    TextContext text = new TextContext("Peri", "Ini adalah cerita peri", "Cerpen", "Low");

    assertEquals("Peri", text.getTitle());
    assertEquals("Ini adalah cerita peri", text.getContent());
    assertEquals("Cerpen", text.getCategory());
    assertEquals("Low", text.getDifficultyLevel());
  }
}
