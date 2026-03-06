package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.entity.UserProgress;
import id.ac.ui.cs.advprog.yomu.event.QuizCompletionEvent;
import id.ac.ui.cs.advprog.yomu.repository.ReadingRepository;
import id.ac.ui.cs.advprog.yomu.repository.UserProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuizServiceTest {

  private ReadingRepository readingRepository;
  private UserProgressRepository userProgressRepository;
  private ApplicationEventPublisher eventPublisher;
  private QuizService quizService;

  @BeforeEach
  void setUp() {
    readingRepository = mock(ReadingRepository.class);
    userProgressRepository = mock(UserProgressRepository.class);
    eventPublisher = mock(ApplicationEventPublisher.class);
    quizService = new QuizService(readingRepository,
        userProgressRepository, eventPublisher);
  }

  @Test
  void testGetReadingAlreadyCompleted() {
    String userId = "user1";
    String readingId = "read1";

    when(userProgressRepository.existsByUserIdAndReadingId(userId,
        readingId)).thenReturn(true);

    IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
        quizService.getReading(userId, readingId)
    );
    assertEquals("Congratulations! You've completed this quiz!",
        exception.getMessage());
  }

  @Test
  void testGetReadingNotCompleted() {
    String userId = "user1";
    String readingId = "read1";
    Reading reading = new Reading();
    reading.setId(readingId);

    when(userProgressRepository.existsByUserIdAndReadingId(userId,
        readingId)).thenReturn(false);
    when(readingRepository.findById(readingId)).
        thenReturn(Optional.of(reading));

    Reading result = quizService.getReading(userId,
        readingId);
    assertEquals(reading, result);
  }

  @Test
  void testCompleteQuizSuccess() {
    String userId = "user1";
    String readingId = "read1";

    when(userProgressRepository.existsByUserIdAndReadingId(userId,
        readingId)).thenReturn(false);

    quizService.completeQuiz(userId, readingId);

    // verifikasi progress disimpan
    ArgumentCaptor<UserProgress> progressCaptor = ArgumentCaptor.
        forClass(UserProgress.class);
    verify(userProgressRepository, times(1)).
        save(progressCaptor.capture());
    assertEquals(userId, progressCaptor.getValue().getUserId());
    assertEquals(readingId, progressCaptor.getValue().getReadingId());
    assertNotNull(progressCaptor.getValue().getCompletedAt());

    // verifikasi event dipublish
    ArgumentCaptor<QuizCompletionEvent> eventCaptor = ArgumentCaptor.
        forClass(QuizCompletionEvent.class);
    verify(eventPublisher, times(1)).
        publishEvent(eventCaptor.capture());
    assertEquals(userId, eventCaptor.getValue().getUserId());
    assertEquals(readingId, eventCaptor.getValue().getReadingId());
  }

  @Test
  void testCompleteQuizAlreadyCompleted() {
    String userId = "user1";
    String readingId = "read1";

    when(userProgressRepository.existsByUserIdAndReadingId
        (userId, readingId)).thenReturn(true);

    IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
        quizService.completeQuiz(userId, readingId)
    );
    assertEquals("This quiz has been completed",
        exception.getMessage());

    verify(userProgressRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void testGetReadingNotFound() {
    String userId = "user1";
    String readingId = "read1";

    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId)).
        thenReturn(false);
    when(readingRepository.findById(readingId)).thenReturn(Optional.empty());

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class, () ->
        quizService.getReading(userId, readingId)
    );
    assertEquals("Not found", exception.getMessage());
  }
}