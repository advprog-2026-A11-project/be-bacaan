package id.ac.ui.cs.advprog.yomu.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuizCompletionEventTest {

  @Test
  void testConstructorValidIds() {
    String userId = "user123";
    String readingId = "reading-456";

    QuizCompletionEvent event = new QuizCompletionEvent(this, userId, readingId);

    assertEquals(userId, event.getUserId());
    assertEquals(readingId, event.getReadingId());
    assertNotNull(event.getSource());
    assertSame(this, event.getSource());
  }

  @Test
  void testConstructorInvalidUserId() {
    String invalidUserId = "user 123!";
    String readingId = "reading-456";

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> new QuizCompletionEvent(this, invalidUserId, readingId));

    assertTrue(ex.getMessage().contains("Invalid userId format"));
  }

  @Test
  void testConstructorInvalidReadingId() {
    String userId = "user123";
    String invalidReadingId = "reading 456!";

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> new QuizCompletionEvent(this, userId, invalidReadingId));

    assertEquals("Invalid readingId format", ex.getMessage());
  }

  @Test
  void testConstructorNullIds() {
    // userId null
    assertThrows(IllegalArgumentException.class,
        () -> new QuizCompletionEvent(this, null, "reading-123"));

    // readingId null
    assertThrows(IllegalArgumentException.class,
        () -> new QuizCompletionEvent(this, "user123", null));
  }
}