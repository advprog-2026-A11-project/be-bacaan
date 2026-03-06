package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.entity.UserProgress;
import id.ac.ui.cs.advprog.yomu.event.QuizCompletionEvent;
import id.ac.ui.cs.advprog.yomu.repository.ReadingRepository;
import id.ac.ui.cs.advprog.yomu.repository.UserProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuizServiceTest {

  @Mock
  private ReadingRepository readingRepository;

  @Mock
  private UserProgressRepository userProgressRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private QuizService quizService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  // =============================
  // validateId() tests
  // =============================
  @Test
  void testValidateId_Null() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> quizService.getReading(null, "reading123"));
    assertEquals("Invalid ID format", ex.getMessage());
  }

  @Test
  void testValidateId_InvalidChars() {
    assertThrows(IllegalArgumentException.class, () -> quizService
        .getReading("user 123!", "reading123"));
    assertThrows(IllegalArgumentException.class, () -> quizService
        .getReading("user123", "reading 123!"));
  }

  // =============================
  // getReading() tests
  // =============================
  @Test
  void testGetReading_Success() {
    String userId = "user123";
    String readingId = "reading456";

    Reading reading = new Reading();
    reading.setId(readingId);
    reading.setTitle("Sample Reading");

    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(false);
    when(readingRepository.findById(readingId)).thenReturn(Optional.of(reading));

    Reading result = quizService.getReading(userId, readingId);

    assertNotNull(result);
    assertEquals(readingId, result.getId());
    assertEquals("Sample Reading", result.getTitle());

    verify(userProgressRepository, times(1))
        .existsByUserIdAndReadingId(userId, readingId);
    verify(readingRepository, times(1)).findById(readingId);
  }

  @Test
  void testGetReading_AlreadyCompleted() {
    String userId = "user123";
    String readingId = "reading456";

    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(true);

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> quizService.getReading(userId, readingId));
    assertEquals("Congratulations! You've completed this quiz!", ex.getMessage());

    verify(readingRepository, never()).findById(anyString());
  }

  @Test
  void testGetReading_ReadingNotFound() {
    String userId = "user123";
    String readingId = "reading456";

    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(false);
    when(readingRepository.findById(readingId)).thenReturn(Optional.empty());

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> quizService.getReading(userId, readingId));
    assertEquals("Reading not found", ex.getMessage());
  }

  // =============================
  // completeQuiz() tests
  // =============================
  @Test
  void testCompleteQuiz_Success() {
    String userId = "user123";
    String readingId = "reading-456";

    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(false);

    quizService.completeQuiz(userId, readingId);

    // Verifikasi UserProgress disimpan
    ArgumentCaptor<UserProgress> captor = ArgumentCaptor
        .forClass(UserProgress.class);
    verify(userProgressRepository, times(1))
        .save(captor.capture());

    UserProgress saved = captor.getValue();
    assertEquals(userId, saved.getUserId());
    assertEquals(readingId, saved.getReadingId());
    assertNotNull(saved.getCompletedAt());

    // Verifikasi event diterbitkan
    ArgumentCaptor<QuizCompletionEvent> eventCaptor = ArgumentCaptor
        .forClass(QuizCompletionEvent.class);
    verify(eventPublisher, times(1))
        .publishEvent(eventCaptor.capture());

    QuizCompletionEvent publishedEvent = eventCaptor.getValue();
    assertEquals(userId, publishedEvent.getUserId());
    assertEquals(readingId, publishedEvent.getReadingId());
  }

  @Test
  void testCompleteQuiz_AlreadyCompleted() {
    String userId = "user123";
    String readingId = "reading-456";

    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(true);

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> quizService.completeQuiz(userId, readingId));
    assertEquals("This quiz has been completed", ex.getMessage());

    verify(userProgressRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void testCompleteQuiz_InvalidId() {
    assertThrows(IllegalArgumentException.class, () -> quizService
        .completeQuiz("user 123!", "reading-456"));
    assertThrows(IllegalArgumentException.class, () -> quizService
        .completeQuiz("user123", "reading 456!"));
    assertThrows(IllegalArgumentException.class, () -> quizService
        .completeQuiz(null, "reading-456"));
    assertThrows(IllegalArgumentException.class, () -> quizService
        .completeQuiz("user123", null));
  }
}