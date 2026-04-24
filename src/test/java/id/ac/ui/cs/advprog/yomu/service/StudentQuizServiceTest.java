package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitRequest;
import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitResponse;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.repository.QuizRepository;
import id.ac.ui.cs.advprog.yomu.repository.UserProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentQuizServiceTest {

  @Mock
  private QuizRepository quizRepository;

  @Mock
  private UserProgressRepository userProgressRepository;

  @Mock
  private QuizService quizService;

  @InjectMocks
  private StudentQuizService studentQuizService;

  private Reading reading;
  private Question questionMultipleChoice;
  private Question questionTrueFalse;

  @BeforeEach
  void setUp() {
    reading = new Reading();
    reading.setId("reading-abc");
    reading.setTitle("Sejarah Komputer");

    questionMultipleChoice = new Question();
    questionMultipleChoice.setId("q-001");
    questionMultipleChoice.setText("Apa kepanjangan dari CPU?");
    questionMultipleChoice.setQuestionType("MULTIPLE_CHOICE");
    questionMultipleChoice.setOptions(List.of(
        "Central Processing Unit",
        "Computer Power Unit",
        "Control Processing Unit"
    ));
    questionMultipleChoice.setCorrectAnswer("A");
    questionMultipleChoice.setReading(reading);

    questionTrueFalse = new Question();
    questionTrueFalse.setId("q-002");
    questionTrueFalse.setText("Matahari terbit dari barat.");
    questionTrueFalse.setQuestionType("TRUE_FALSE");
    questionTrueFalse.setOptions(List.of("True", "False"));
    questionTrueFalse.setCorrectAnswer("False");
    questionTrueFalse.setReading(reading);
  }

  @Test
  void testGetQuizQuestionsWhenNotCompletedReturnListOfQuestions() {
    String userId = "user-123";
    String readingId = "reading-abc";

    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(false);  // belum pernah dikerjakan

    when(quizRepository.findByReadingId(readingId))
        .thenReturn(List.of(questionMultipleChoice, questionTrueFalse));

    // WHEN: student minta soal
    List<Question> result = studentQuizService.getQuizQuestion(userId, readingId);

    // THEN: harus dapat 2 soal
    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyInAnyOrder(questionMultipleChoice, questionTrueFalse);

    verify(userProgressRepository).existsByUserIdAndReadingId(userId, readingId);
    verify(quizRepository).findByReadingId(readingId);
  }

  @Test
  void testGetQuizQuestionsWhenAlreadyCompleted() {
    String userId = "user-123";
    String readingId = "reading-abc";

    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(true);  // sudah pernah dikerjakan!

    // throw exception, jangan kasih soal lagi
    assertThatThrownBy(() ->
        studentQuizService.getQuizQuestion(userId, readingId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("You've completed this quiz");

    verify(quizRepository, never()).findByReadingId(anyString());
  }

  @Test
  void testGetQuizQuestionsWhenNoQuestionsExist() {
    String userId = "user-123";
    String readingId = "reading-abc";

    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(false);
    when(quizRepository.findByReadingId(readingId))
        .thenReturn(List.of());  // kosong

    // WHEN
    List<Question> result = studentQuizService.getQuizQuestion(userId, readingId);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  void testSubmitQuizWithAllCorrectAnswers() {
    String userId = "user-123";
    String readingId = "reading-abc";

    QuizSubmitRequest request = new QuizSubmitRequest();
    Map<String, String> answers = Map.of(
        "q-001", "A",
        "q-002", "False"
    );
    request.setAnswers(answers);
    request.setTimeTakenSeconds(120);

    when(quizRepository.findByReadingId(readingId))
        .thenReturn(List.of(questionMultipleChoice, questionTrueFalse));

    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(false);

    QuizSubmitResponse response = studentQuizService.submitQuiz(userId, readingId, request);

    assertThat(response).isNotNull();
    assertThat(response.getTotalQuestions()).isEqualTo(2);
    assertThat(response.getCorrectAnswers()).isEqualTo(2);
    assertThat(response.getScore()).isEqualTo(100);
    assertThat(response.getAccuracy()).isEqualTo(1.0);

    // progress harus disimpan setelah submit
    verify(quizService).completeQuiz(userId, readingId, 100, 1.0);
  }

  @Test
  void testSubmitQuizWithHalfCorrectAnswers() {
    String userId = "user-456";
    String readingId = "reading-abc";

    QuizSubmitRequest request = new QuizSubmitRequest();
    Map<String, String> answers = Map.of(
        "q-001", "A",
        "q-002", "True"   // the correct answer is False
    );
    request.setAnswers(answers);
    request.setTimeTakenSeconds(90);

    when(quizRepository.findByReadingId(readingId))
        .thenReturn(List.of(questionMultipleChoice, questionTrueFalse));
    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(false);

    QuizSubmitResponse response = studentQuizService.submitQuiz(userId, readingId, request);

    assertThat(response.getTotalQuestions()).isEqualTo(2);
    assertThat(response.getCorrectAnswers()).isEqualTo(1);
    assertThat(response.getScore()).isEqualTo(50);
    assertThat(response.getAccuracy()).isEqualTo(0.5);

    verify(quizService).completeQuiz(userId, readingId, 50, 0.5);
  }

  @Test
  void testSubmitQuiz_WithAllWrongAnswers() {
    String userId = "user-789";
    String readingId = "reading-abc";

    QuizSubmitRequest request = new QuizSubmitRequest();
    Map<String, String> answers = Map.of(
        "q-001", "B",
        "q-002", "True"
    );

    request.setAnswers(answers);
    request.setTimeTakenSeconds(60);

    when(quizRepository.findByReadingId(readingId))
        .thenReturn(List.of(questionMultipleChoice, questionTrueFalse));
    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(false);

    QuizSubmitResponse response = studentQuizService.submitQuiz(userId, readingId, request);

    assertThat(response.getCorrectAnswers()).isEqualTo(0);
    assertThat(response.getScore()).isEqualTo(0);
    assertThat(response.getAccuracy()).isEqualTo(0);

    verify(quizService).completeQuiz(userId, readingId, 0, 0.0);
  }

  @Test
  void testSubmitQuizSaveProgress() {
    String userId = "user-111";
    String readingId = "reading-abc";

    QuizSubmitRequest request = new QuizSubmitRequest();
    request.setAnswers(Map.of("q-001", "A"));
    request.setTimeTakenSeconds(30);

    when(quizRepository.findByReadingId(readingId))
        .thenReturn(List.of(questionMultipleChoice));
    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(false);

    studentQuizService.submitQuiz(userId, readingId, request);

    verify(quizService, times(1)).completeQuiz(
        eq(userId),
        eq(readingId),
        anyInt(),
        anyDouble()
    );
  }

  @Test
  void testSubmitQuizResponseShouldContainResults() {
    String userId = "user-222";
    String readingId = "reading-abc";

    QuizSubmitRequest request = new QuizSubmitRequest();
    request.setAnswers(Map.of(
        "q-001", "A",
        "q-002", "True"
    ));
    request.setTimeTakenSeconds(45);

    when(quizRepository.findByReadingId(readingId))
        .thenReturn(List.of(questionMultipleChoice, questionTrueFalse));
    when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
        .thenReturn(false);

    QuizSubmitResponse response = studentQuizService.submitQuiz(userId, readingId, request);

    assertThat(response.getCorrectAnswers()).isNotNull();
    assertThat(response.getQuestionResults()).containsEntry("q-001", true);
    assertThat(response.getQuestionResults()).containsEntry("q-002", false);
  }
}
