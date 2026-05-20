package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.dto.QuizResultResponse;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.entity.UserProgress;
import id.ac.ui.cs.advprog.yomu.event.QuizCompletionEvent;
import id.ac.ui.cs.advprog.yomu.repository.QuizRepository;
import id.ac.ui.cs.advprog.yomu.repository.ReadingRepository;
import id.ac.ui.cs.advprog.yomu.repository.UserProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private QuizRepository quizRepository;

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
  void testValidateIdNull() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> quizService.getReading(null, "reading123"));
    assertEquals("Invalid ID format", ex.getMessage());
  }

  @Test
  void testValidateIdInvalidChars() {
    assertThrows(IllegalArgumentException.class, () -> quizService
        .getReading("user 123!", "reading123"));
    assertThrows(IllegalArgumentException.class, () -> quizService
        .getReading("user123", "reading 123!"));
  }

  // =============================
  // getReading() tests
  // =============================
  @Test
  void testGetReadingSuccess() {
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
  void testGetReadingAlreadyCompleted() {
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
  void testGetReadingReadingNotFound() {
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
  void testCompleteQuizSuccess() {
    String userId = "user123";
    String readingId = "reading-456";
    int score = 80;
    double accuracy = 0.9;

    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(false);

    quizService.completeQuiz(userId, readingId, score, accuracy);

    ArgumentCaptor<UserProgress> captor = ArgumentCaptor
        .forClass(UserProgress.class);

    verify(userProgressRepository, times(1))
        .save(captor.capture());

    UserProgress saved = captor.getValue();
    assertEquals(userId, saved.getUserId());
    assertEquals(readingId, saved.getReadingId());
    assertEquals(score, saved.getScore());
    assertEquals(accuracy, saved.getAccuracy());
    assertNotNull(saved.getCompletedAt());

    ArgumentCaptor<QuizCompletionEvent> eventCaptor = ArgumentCaptor
        .forClass(QuizCompletionEvent.class);

    verify(eventPublisher, times(1))
        .publishEvent(eventCaptor.capture());

    QuizCompletionEvent publishedEvent = eventCaptor.getValue();
    assertEquals(userId, publishedEvent.getUserId());
    assertEquals(readingId, publishedEvent.getReadingId());
  }

  @Test
  void testCompleteQuizWithUserAnswersSavesAnswers() {
    String userId = "user123";
    String readingId = "reading-456";
    Map<String, String> answers = Map.of("q1", "A", "q2", "True");

    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(false);

    quizService.completeQuiz(userId, readingId, 80, 0.8, answers);

    ArgumentCaptor<UserProgress> captor = ArgumentCaptor.forClass(UserProgress.class);
    verify(userProgressRepository).save(captor.capture());

    UserProgress saved = captor.getValue();
    assertEquals("A", saved.getUserAnswers().get("q1"));
    assertEquals("True", saved.getUserAnswers().get("q2"));
  }

  @Test
  void testCompleteQuizAlreadyCompleted() {
    String userId = "user123";
    String readingId = "reading-456";
    int score = 80;
    double accuracy = 0.9;

    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(true);

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> quizService.completeQuiz(userId, readingId, score, accuracy));

    assertEquals("This quiz has been completed", ex.getMessage());

    verify(userProgressRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void testCompleteQuizInvalidId() {
    int score = 80;
    double accuracy = 0.9;

    assertThrows(IllegalArgumentException.class, () -> quizService
        .completeQuiz("user 123!", "reading-456", score, accuracy));
    assertThrows(IllegalArgumentException.class, () -> quizService
        .completeQuiz("user123", "reading 456!", score, accuracy));
    assertThrows(IllegalArgumentException.class, () -> quizService
        .completeQuiz(null, "reading-456", score, accuracy));
    assertThrows(IllegalArgumentException.class, () -> quizService
        .completeQuiz("user123", null, score, accuracy));
  }

  @Test
  void testCompleteQuizStillSucceedsWhenAchievementServiceFails() {
    String userId = "user-123";
    String readingId = "reading-456";

    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(false);

    when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
        .thenThrow(new RuntimeException("Achievement service down!"));

    assertDoesNotThrow(() -> quizService.completeQuiz(userId, readingId, 80, 0.8));
    verify(userProgressRepository, times(1)).save(any(UserProgress.class));
  }

  // =============================
  // getQuizResult() tests
  // =============================
  @Test
  void testGetQuizResultSuccess() {
    String userId = "user123";
    String readingId = "reading-456";

    Question q1 = new Question();
    q1.setId("q1");
    q1.setText("Apa ibu kota Indonesia?");
    q1.setQuestionType("MULTIPLE_CHOICE");
    q1.setOptions(List.of("Jakarta", "Bandung", "Surabaya"));
    q1.setCorrectAnswer("Jakarta");

    Question q2 = new Question();
    q2.setId("q2");
    q2.setText("Bumi berbentuk bulat?");
    q2.setQuestionType("TRUE_FALSE");
    q2.setOptions(List.of("True", "False"));
    q2.setCorrectAnswer("True");

    UserProgress progress = new UserProgress();
    progress.setUserId(userId);
    progress.setReadingId(readingId);
    progress.setScore(50);
    progress.setAccuracy(0.5);
    progress.setCompletedAt(LocalDateTime.now());
    progress.setUserAnswers(Map.of("q1", "Jakarta", "q2", "False"));

    when(userProgressRepository.findByUserIdAndReadingId(userId, readingId))
        .thenReturn(Optional.of(progress));
    when(quizRepository.findByReadingId(readingId))
        .thenReturn(List.of(q1, q2));

    QuizResultResponse result = quizService.getQuizResult(userId, readingId);

    assertEquals(readingId, result.getReadingId());
    assertEquals(50, result.getScore());
    assertEquals(0.5, result.getAccuracy());
    assertEquals(2, result.getTotalQuestions());
    assertEquals(1, result.getCorrectAnswers());
    assertEquals(2, result.getQuestionDetails().size());

    QuizResultResponse.QuestionResultDetail detail1 = result.getQuestionDetails().get(0);
    assertEquals("q1", detail1.getQuestionId());
    assertEquals("Jakarta", detail1.getUserAnswer());
    assertEquals("Jakarta", detail1.getCorrectAnswer());
    assertTrue(detail1.isCorrect());

    QuizResultResponse.QuestionResultDetail detail2 = result.getQuestionDetails().get(1);
    assertEquals("q2", detail2.getQuestionId());
    assertEquals("False", detail2.getUserAnswer());
    assertEquals("True", detail2.getCorrectAnswer());
    assertFalse(detail2.isCorrect());
  }

  @Test
  void testGetQuizResultNotCompletedYet() {
    String userId = "user123";
    String readingId = "reading-456";

    when(userProgressRepository.findByUserIdAndReadingId(userId, readingId))
        .thenReturn(Optional.empty());

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> quizService.getQuizResult(userId, readingId));

    assertEquals("Quiz result not found. User has not completed this quiz.", ex.getMessage());
  }

  @Test
  void testGetQuizResultInvalidUserId() {
    assertThrows(IllegalArgumentException.class,
        () -> quizService.getQuizResult("user 123!", "reading-456"));
    assertThrows(IllegalArgumentException.class,
        () -> quizService.getQuizResult(null, "reading-456"));
  }

  @Test
  void testGetQuizResultNullAnswerTreatedAsIncorrect() {
    String userId = "user123";
    String readingId = "reading-456";

    Question q1 = new Question();
    q1.setId("q1");
    q1.setText("Pertanyaan 1");
    q1.setQuestionType("MULTIPLE_CHOICE");
    q1.setOptions(List.of("A", "B", "C"));
    q1.setCorrectAnswer("A");

    UserProgress progress = new UserProgress();
    progress.setUserId(userId);
    progress.setReadingId(readingId);
    progress.setScore(0);
    progress.setAccuracy(0.0);
    progress.setCompletedAt(LocalDateTime.now());
    // user tidak menjawab q1
    progress.setUserAnswers(Map.of());

    when(userProgressRepository.findByUserIdAndReadingId(userId, readingId))
        .thenReturn(Optional.of(progress));
    when(quizRepository.findByReadingId(readingId))
        .thenReturn(List.of(q1));

    QuizResultResponse result = quizService.getQuizResult(userId, readingId);

    assertEquals(0, result.getCorrectAnswers());
    assertNull(result.getQuestionDetails().get(0).getUserAnswer());
    assertFalse(result.getQuestionDetails().get(0).isCorrect());
  }
}