package id.ac.ui.cs.advprog.yomu.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuizCompletedEventTest {

  @Test
  void defaultConstructorShouldCreateEmptyEvent() {
    QuizCompletedEvent event = new QuizCompletedEvent();

    assertNull(event.getEventId());
    assertNull(event.getUserId());
    assertNull(event.getReadingId());
    assertNull(event.getCategory());
    assertNull(event.getDifficultyLevel());
    assertEquals(0, event.getScore());
    assertEquals(0, event.getAccuracy());
  }
}
