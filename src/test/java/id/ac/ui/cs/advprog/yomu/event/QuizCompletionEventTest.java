package id.ac.ui.cs.advprog.yomu.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuizCompletionEventTest {

  @Test
  void testEventCreationAndGetters() {
    // given
    String expectedUserId = "user123";
    String expectedReadingId = "reading456";
    Object expectedSource = new Object();

    // when
    QuizCompletionEvent event = new QuizCompletionEvent(expectedSource,
        expectedUserId, expectedReadingId);

    // then
    assertNotNull(event, "Event should be created");
    assertEquals(expectedUserId, event.getUserId(), "User ID should match");
    assertEquals(expectedReadingId, event.getReadingId(), "Reading ID should match");
    assertEquals(expectedSource, event.getSource(), "Source should match");
  }
}