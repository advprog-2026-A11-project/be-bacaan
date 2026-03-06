package id.ac.ui.cs.advprog.yomu.controller;

import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReadingControllerTest {

  @Mock
  private QuizService quizService;

  @InjectMocks
  private ReadingController readingController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  // =============================
  // GET /{readingId}
  // =============================
  @Test
  void testGetReadingSuccess() {
    String userId = "user123";
    String readingId = "reading456";

    Reading reading = new Reading();
    reading.setId(readingId);
    reading.setTitle("Sample Reading");

    when(quizService.getReading(userId, readingId)).thenReturn(reading);

    ResponseEntity<Reading> response = readingController.getReading(userId, readingId);

    assertEquals(200, response.getStatusCodeValue());
    assertEquals("reading456", response.getBody().getId());
    assertEquals("Sample Reading", response.getBody().getTitle());

    verify(quizService, times(1)).getReading(userId, readingId);
  }

  @Test
  void testGetReadingServiceThrowsException() {
    String userId = "user123";
    String readingId = "reading456";

    when(quizService.getReading(userId, readingId))
        .thenThrow(new IllegalStateException("Quiz already completed"));

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> readingController.getReading(userId, readingId));

    assertEquals("Quiz already completed", ex.getMessage());
    verify(quizService, times(1)).getReading(userId, readingId);
  }

  // =============================
  // POST /{readingId}/complete
  // =============================
  @Test
  void testCompleteQuizSuccess() {
    String userId = "user123";
    String readingId = "reading456";

    ResponseEntity<String> response = readingController.completeQuiz(userId, readingId);

    assertEquals(200, response.getStatusCodeValue());
    assertEquals("Thank you for completing the quiz!", response.getBody());
    verify(quizService, times(1)).completeQuiz(userId, readingId);
  }

  @Test
  void testCompleteQuizInvalidUserId() {
    String invalidUserId = "user 123!"; // ada spasi & simbol
    String readingId = "reading456";

    ResponseEntity<String> response = readingController.completeQuiz(invalidUserId, readingId);

    assertEquals(400, response.getStatusCodeValue());
    assertEquals("Invalid User ID format", response.getBody());
    verify(quizService, never()).completeQuiz(anyString(), anyString());
  }

  @Test
  void testCompleteQuizNullUserId() {
    String readingId = "reading456";

    ResponseEntity<String> response = readingController.completeQuiz(null, readingId);

    assertEquals(400, response.getStatusCodeValue());
    assertEquals("Invalid User ID format", response.getBody());
    verify(quizService, never()).completeQuiz(anyString(), anyString());
  }

  @Test
  void testCompleteQuizInvalidReadingId() {
    String userId = "user123";
    String invalidReadingId = "reading 456!";

    ResponseEntity<String> response = readingController.completeQuiz(userId, invalidReadingId);

    assertEquals(400, response.getStatusCodeValue());
    assertEquals("Invalid Reading ID format", response.getBody());
    verify(quizService, never()).completeQuiz(anyString(), anyString());
  }

  @Test
  void testCompleteQuizNullReadingId() {
    String userId = "user123";

    ResponseEntity<String> response = readingController.completeQuiz(userId, null);

    assertEquals(400, response.getStatusCodeValue());
    assertEquals("Invalid Reading ID format", response.getBody());
    verify(quizService, never()).completeQuiz(anyString(), anyString());
  }

  @Test
  void testCompleteQuizServiceThrowsException() {
    String userId = "user123";
    String readingId = "reading456";

    doThrow(new IllegalStateException("This quiz has been completed"))
        .when(quizService).completeQuiz(userId, readingId);

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> readingController.completeQuiz(userId, readingId));

    assertEquals("This quiz has been completed", ex.getMessage());
    verify(quizService, times(1)).completeQuiz(userId, readingId);
  }
}