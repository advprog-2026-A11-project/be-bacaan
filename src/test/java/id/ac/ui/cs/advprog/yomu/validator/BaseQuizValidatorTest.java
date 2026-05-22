package id.ac.ui.cs.advprog.yomu.validator;

import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseQuizValidatorTest {

  private TestableBaseQuizValidator validator;

  @BeforeEach
  void setUp() {
    validator = new TestableBaseQuizValidator();
  }

  @Test
  void validateQuestionTextValidText() {
    assertDoesNotThrow(() ->
        validator.callValidateQuestionText("How are you?")
    );
  }

  @Test
  void validateQuestionTextNullText() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.callValidateQuestionText(null)
    );

    assertEquals("Question text cannot be empty", exception.getMessage());
  }

  @Test
  void validateQuestionTextEmptyText() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.callValidateQuestionText("")
    );

    assertEquals("Question text cannot be empty", exception.getMessage());
  }

  @Test
  void validateQuestionTextBlankText() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.callValidateQuestionText("   ")
    );

    assertEquals("Question text cannot be empty", exception.getMessage());
  }

  @Test
  void validateQuestionTextTooShort() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.callValidateQuestionText("Hi")
    );

    assertEquals(
        "Question text must be at least 5 characters",
        exception.getMessage()
    );
  }

  @Test
  void validateQuestionTextToLong() {
    String longText = "a".repeat(501);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.callValidateQuestionText(longText)
    );

    assertEquals(
        "Question text cannot exceed 500 characters",
        exception.getMessage()
    );
  }

  @Test
  void validateCommonValidRequest() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setText("What is polymorphism?");
    request.setQuestionType("MCQ");

    assertDoesNotThrow(() ->
        validator.callValidateCommon(request)
    );
  }

  @Test
  void validateCommonNullQuestionType() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setText("What is encapsulation?");
    request.setQuestionType(null);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.callValidateCommon(request)
    );

    assertEquals("Quiz type cannot be empty", exception.getMessage());
  }

  @Test
  void validateCommonEmptyQuestionType() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setText("What is abstraction?");
    request.setQuestionType("");

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.callValidateCommon(request)
    );

    assertEquals("Quiz type cannot be empty", exception.getMessage());
  }

  // Helper subclass for testing protected methods
  static class TestableBaseQuizValidator extends BaseQuizValidator {

    public void callValidateQuestionText(String text) {
      validateQuestionText(text);
    }

    public void callValidateCommon(QuizQuestionRequest request) {
      validateCommon(request);
    }
  }
}

