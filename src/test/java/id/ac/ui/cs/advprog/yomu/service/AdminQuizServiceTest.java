package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.repository.QuizRepository;
import id.ac.ui.cs.advprog.yomu.repository.ReadingRepository;
import id.ac.ui.cs.advprog.yomu.validator.QuizValidator;
import id.ac.ui.cs.advprog.yomu.validator.QuizValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;

class AdminQuizServiceTest {

  @Mock
  private QuizRepository quizRepository;

  @Mock
  private ReadingRepository readingRepository;

  @Mock
  private QuizValidatorFactory validatorFactory;

  @Mock
  private QuizValidator validator;

  @InjectMocks
  private AdminQuizService service;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void addQuestion_shouldSaveQuestionSuccessfully() {

    String readingId = "reading-1";

    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setQuestionType("ESSAY");

    Reading reading = new Reading();

    Question question = new Question();

    when(readingRepository.existsById(readingId)).thenReturn(true);

    when(validatorFactory.getValidator("ESSAY")).thenReturn(validator);

    when(readingRepository.findById(readingId)).thenReturn(Optional.of(reading));

    when(validator.createQuestion(request, reading)).thenReturn(question);

    when(quizRepository.save(question)).thenReturn(question);

    Question result = service.addQuestion(readingId, request);

    assertNotNull(result);
    verify(validator).validate(request);
    verify(quizRepository).save(question);
  }

  @Test
  void addQuestion_whenReadingNotFound_shouldThrowException() {

    when(readingRepository.existsById("invalid")).thenReturn(false);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> service.addQuestion("invalid", new QuizQuestionRequest()));

    assertEquals("Reading not found with id: invalid", exception.getMessage());
  }

  @Test
  void updateQuestion_shouldUpdateTextAndSave() {
    Question question = new Question();
    question.setQuestionType("ESSAY");
    question.setText("Old Text");

    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setText("Updated Question");
    request.setQuestionType("ESSAY");
    String questionId = "q1";

    when(quizRepository.findById(questionId)).thenReturn(Optional.of(question));

    when(validatorFactory.getValidator("ESSAY")).thenReturn(validator);

    doNothing().when(validator).updateQuestion(any(Question.class), any(QuizQuestionRequest.class));

    service.updateQuestion(questionId, request);

    assertEquals("Updated Question", question.getText());
    verify(validator).updateQuestion(question, request);
    verify(quizRepository).save(question);
  }

  @Test
  void updateQuestion_withNullText_shouldNotUpdateText() {
    Question question = new Question();
    question.setQuestionType("ESSAY");
    question.setText("Original Text");

    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setText(null);
    request.setQuestionType("ESSAY");
    String questionId = "q1";

    when(quizRepository.findById(questionId)).thenReturn(Optional.of(question));
    when(validatorFactory.getValidator("ESSAY")).thenReturn(validator);

    service.updateQuestion(questionId, request);

    assertEquals("Original Text", question.getText());
  }

  @Test
  void updateQuestion_withEmptyQuestionType_shouldNotChangeType() {
    Question question = new Question();
    question.setQuestionType("ESSAY");

    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setQuestionType("");
    String questionId = "q1";

    when(quizRepository.findById(questionId)).thenReturn(Optional.of(question));
    when(validatorFactory.getValidator("ESSAY")).thenReturn(validator);

    service.updateQuestion(questionId, request);

    assertEquals("ESSAY", question.getQuestionType());
  }

  @Test
  void updateQuestion_whenQuestionTypeChanges_shouldUseNewValidator() {
    Question question = new Question();
    question.setQuestionType("ESSAY");
    question.setText("Existing Question");

    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setQuestionType("TRUE_FALSE");

    QuizValidator newValidator = mock(QuizValidator.class);
    String questionId = "q1";

    when(quizRepository.findById(questionId)).thenReturn(Optional.of(question));

    when(validatorFactory.getValidator("TRUE_FALSE")).thenReturn(newValidator);

    service.updateQuestion(questionId, request);

    verify(newValidator).validate(request);
    verify(newValidator).updateQuestion(question, request);
    verify(quizRepository).save(question);
    assertEquals("TRUE_FALSE", question.getQuestionType());
  }

  @Test
  void updateQuestion_whenQuestionNotFound_shouldThrowException() {
    when(quizRepository.findById("invalid")).thenReturn(Optional.empty());

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> service.updateQuestion("invalid", new QuizQuestionRequest()));

    assertEquals("Question not found with id: invalid", exception.getMessage());
  }

  @Test
  void updateQuestion_whenQuestionTextTooShort_shouldThrowException() {
    String questionId = "q1";

    Question question = new Question();
    question.setQuestionType("ESSAY");

    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setText("abc");

    when(quizRepository.findById(questionId)).thenReturn(Optional.of(question));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> service.updateQuestion(questionId, request));

    assertEquals("Question text must be at least 5 characters", exception.getMessage());
  }

  @Test
  void updateQuestion_whenQuestionTextEmpty_shouldThrowException() {
    Question question = new Question();
    question.setQuestionType("ESSAY");

    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setText("");
    request.setQuestionType("ESSAY");
    String questionId = "q1";

    when(quizRepository.findById(questionId)).thenReturn(Optional.of(question));
    when(validatorFactory.getValidator("ESSAY")).thenReturn(validator);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> service.updateQuestion(questionId, request));

    assertEquals("Question text cannot be empty", exception.getMessage());
  }

  @Test
  void updateQuestion_withNullQuestionType_shouldNotChangeType() {
    Question question = new Question();
    question.setQuestionType("ESSAY");

    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setQuestionType(null);
    String questionId = "q1";

    when(quizRepository.findById(questionId)).thenReturn(Optional.of(question));
    when(validatorFactory.getValidator("ESSAY")).thenReturn(validator);

    service.updateQuestion(questionId, request);

    assertEquals("ESSAY", question.getQuestionType());
  }

  @Test
  void updateQuestion_whenQuestionTextTooLong_shouldThrowException() {
    Question question = new Question();
    question.setQuestionType("ESSAY");

    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setText("a".repeat(501));
    String questionId = "q1";

    when(quizRepository.findById(questionId)).thenReturn(Optional.of(question));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> service.updateQuestion(questionId, request));

    assertEquals("Question text cannot exceed 500 characters", exception.getMessage());
  }

  @Test
  void deleteQuestion_shouldDeleteSuccessfully() {
    when(quizRepository.existsById("q1")).thenReturn(true);

    service.deleteQuestion("q1");
    verify(quizRepository).deleteById("q1");
  }

  @Test
  void deleteQuestion_whenQuestionNotFound_shouldThrowException() {

    when(quizRepository.existsById("invalid")).thenReturn(false);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> service.deleteQuestion("invalid"));

    assertEquals("Question not found with id: invalid", exception.getMessage());
  }

  @Test
  void deleteAllQuestionsForReading_shouldDeleteAllQuestions() {

    String readingId = "reading-1";

    List<Question> questions = List.of(new Question(), new Question());

    when(readingRepository.existsById(readingId)).thenReturn(true);

    when(quizRepository.findByReadingId(readingId)).thenReturn(questions);

    service.deleteAllQuestionsForReading(readingId);
    verify(quizRepository).deleteAll(questions);
  }

  @Test
  void deleteAllQuestionsForReading_whenNoQuestions_shouldNotDelete() {

    String readingId = "reading-1";

    when(readingRepository.existsById(readingId))
        .thenReturn(true);

    when(quizRepository.findByReadingId(readingId))
        .thenReturn(List.of());

    service.deleteAllQuestionsForReading(readingId);

    verify(quizRepository, never()).deleteAll(any());
  }

  @Test
  void getAllQuestionsForReading_shouldReturnQuestions() {
    String readingId = "reading-1";

    List<Question> questions = List.of(new Question(), new Question());

    when(readingRepository.existsById(readingId)).thenReturn(true);

    when(quizRepository.findByReadingId(readingId)).thenReturn(questions);

    List<Question> result = service.getAllQuestionsForReading(readingId);
    assertEquals(2, result.size());
  }

  @Test
  void getQuestion_shouldReturnQuestion() {
    Question question = new Question();

    when(quizRepository.findById("q1")).thenReturn(Optional.of(question));

    Question result = service.getQuestion("q1");
    assertNotNull(result);
  }

  @Test
  void getQuestionCountForReading_shouldReturnCount() {
    String readingId = "reading-1";

    when(readingRepository.existsById(readingId)).thenReturn(true);

    when(quizRepository.countByReadingId(readingId)).thenReturn(5L);

    long result = service.getQuestionCountForReading(readingId);
    assertEquals(5L, result);
  }

  @Test
  void findReadingById_whenFoundThenDisappears_shouldThrowException() {
    String readingId = "reading-1";
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setQuestionType("ESSAY");

    when(readingRepository.existsById(readingId)).thenReturn(true);
    when(validatorFactory.getValidator("ESSAY")).thenReturn(validator);
    when(readingRepository.findById(readingId)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> service.addQuestion(readingId, request));
  }
}
