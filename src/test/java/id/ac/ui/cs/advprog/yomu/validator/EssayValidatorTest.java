package id.ac.ui.cs.advprog.yomu.validator;

import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EssayValidatorTest {

  private EssayValidator validator;

  @BeforeEach
  void setUp() {
    validator = new EssayValidator();
  }

  @Test
  void getQuestionTypeTest() {
    assertEquals("ESSAY", validator.getQuestionType());
  }

  @Test
  void validateValidRequestTest() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("This is a valid essay answer.");
    request.setOptions(List.of());

    assertDoesNotThrow(() -> validator.validate(request));
  }

  @Test
  void validateNullCorrectAnswer() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer(null);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(request)
    );

    assertEquals(
        "Sample answer cannot be empty for essay question",
        exception.getMessage()
    );
  }

  @Test
  void validateEemptyCorrectAnswer() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("");

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(request)
    );

    assertEquals(
        "Sample answer cannot be empty for essay question",
        exception.getMessage()
    );
  }

  @Test
  void validateBlankCorrectAnswer() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("   ");

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(request)
    );

    assertEquals(
        "Sample answer cannot be empty for essay question",
        exception.getMessage()
    );
  }

  @Test
  void validateCorrectAnswerTooLong() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("a".repeat(1001));

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(request)
    );

    assertEquals(
        "Sample answer cannot exceed 1000 characters",
        exception.getMessage()
    );
  }

  @Test
  void validateOptionsNotEmpty() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("Valid answer");
    request.setOptions(List.of("Option A"));

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(request)
    );

    assertEquals(
        "Essay questions should not have options",
        exception.getMessage()
    );
  }

  @Test
  void validateNullOptionsTest() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("Valid answer");
    request.setOptions(null);

    assertDoesNotThrow(() -> validator.validate(request));
  }

  @Test
  void createQuestionTest() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setText("Explain polymorphism");
    request.setCorrectAnswer(" Polymorphism allows many forms. ");

    Reading reading = new Reading();

    Question question = validator.createQuestion(request, reading);

    assertEquals("Explain polymorphism", question.getText());
    assertEquals("ESSAY", question.getQuestionType());
    assertEquals(List.of(), question.getOptions());
    assertEquals(
        "Polymorphism allows many forms.",
        question.getCorrectAnswer()
    );
    assertEquals(reading, question.getReading());
  }

  @Test
  void updateQuestionValidCorrectAnswer() {
    Question question = new Question();
    question.setCorrectAnswer("Old Answer");

    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer(" Updated Answer ");

    validator.updateQuestion(question, request);

    assertEquals("Updated Answer", question.getCorrectAnswer());
  }

  @Test
  void updateQuestionNullCorrectAnswer() {
    Question question = new Question();
    question.setCorrectAnswer("Original Answer");

    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer(null);

    validator.updateQuestion(question, request);

    assertEquals("Original Answer", question.getCorrectAnswer());
  }

  @Test
  void updateQuestionCorrectAnswerTooLong() {
    Question question = new Question();

    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("a".repeat(1001));

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.updateQuestion(question, request)
    );

    assertEquals(
        "Sample answer cannot exceed 1000 characters",
        exception.getMessage()
    );
  }
}