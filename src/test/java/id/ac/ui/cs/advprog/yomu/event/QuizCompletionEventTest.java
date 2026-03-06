package id.ac.ui.cs.advprog.yomu.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuizCompletionEventTest {

  @Test
  void testConstructor_ValidIds() {
    String userId = "user123";
    String readingId = "reading-456";

    QuizCompletionEvent event = new QuizCompletionEvent(this, userId, readingId);

    assertEquals(userId, event.getUserId());
    assertEquals(readingId, event.getReadingId());
    assertNotNull(event.getSource());
    assertSame(this, event.getSource());
  }

  @Test
  void testConstructor_InvalidUserId() {
    String invalidUserId = "user 123!";
    String readingId = "reading-456";

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> new QuizCompletionEvent(this, invalidUserId, readingId));

    assertTrue(ex.getMessage().contains("Invalid userId format"));
  }

  @Test
  void testConstructor_InvalidReadingId() {
    String userId = "user123";
    String invalidReadingId = "reading 456!";

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> new QuizCompletionEvent(this, userId, invalidReadingId));

    assertEquals("Invalid readingId format", ex.getMessage());
  }

  @Test
  void testConstructor_NullIds() {
    // userId null
    assertThrows(IllegalArgumentException.class,
        () -> new QuizCompletionEvent(this, null, "reading-123"));

    // readingId null
    assertThrows(IllegalArgumentException.class,
        () -> new QuizCompletionEvent(this, "user123", null));
  }
}