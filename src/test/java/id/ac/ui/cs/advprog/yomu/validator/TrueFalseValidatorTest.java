package id.ac.ui.cs.advprog.yomu.validator;

import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.entity.Reading;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrueFalseValidatorTest {

  private TrueFalseValidator validator;

  @BeforeEach
  void setUp() {
    validator = new TrueFalseValidator();
  }

  @Test
  void getQuestionType_shouldReturnTrueFalse() {
    assertEquals("TRUE_FALSE", validator.getQuestionType());
  }

  @Test
  void validateValidTrue() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("True");

    assertDoesNotThrow(() -> validator.validate(request));
  }

  @Test
  void validateValidFalse() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("False");

    assertDoesNotThrow(() -> validator.validate(request));
  }

  @Test
  void validateLowercaseAnswer() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("true");

    assertDoesNotThrow(() -> validator.validate(request));
  }

  @Test
  void validateAnswerWithSpaces() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("  false  ");

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
        "Correct answer cannot be empty for true/false question",
        exception.getMessage()
    );
  }

  @Test
  void validateEmptyCorrectAnswer() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("");

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(request)
    );

    assertEquals(
        "Correct answer cannot be empty for true/false question",
        exception.getMessage()
    );
  }

  @Test
  void validateWhitespaceCorrectAnswer() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("   ");

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(request)
    );

    assertEquals(
        "Correct answer cannot be empty for true/false question",
        exception.getMessage()
    );
  }

  @Test
  void validateInvalidAnswer() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("Maybe");

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(request)
    );

    assertEquals(
        "True/False answer must be 'True' or 'False'",
        exception.getMessage()
    );
  }

  @Test
  void createQuestionTest() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setText("Java is platform independent?");
    request.setCorrectAnswer("true");

    Reading reading = new Reading();

    Question question = validator.createQuestion(request, reading);

    assertEquals("Java is platform independent?", question.getText());
    assertEquals("TRUE_FALSE", question.getQuestionType());
    assertEquals(List.of("True", "False"), question.getOptions());
    assertEquals("True", question.getCorrectAnswer());
    assertEquals(reading, question.getReading());
  }

  @Test
  void createQuestion_withNullAnswer_shouldHandleNull() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer(null);
    Question question = validator.createQuestion(request, new Reading());
    assertNull(question.getCorrectAnswer());
  }

  @Test
  void createQuestionWithEmptyAnswer() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("");
    Question question = validator.createQuestion(request, new Reading());
    assertEquals("", question.getCorrectAnswer());
  }

  @Test
  void updateQuestionValidAnswer() {
    Question question = new Question();
    question.setCorrectAnswer("False");

    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("true");

    validator.updateQuestion(question, request);
    assertEquals("True", question.getCorrectAnswer());
  }

  @Test
  void updateQuestionNullAnswer() {
    Question question = new Question();
    question.setCorrectAnswer("False");

    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer(null);

    validator.updateQuestion(question, request);
    assertEquals("False", question.getCorrectAnswer());
  }

  @Test
  void updateQuestionInvalidAnswer() {
    Question question = new Question();

    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("invalid");

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> validator.updateQuestion(question, request)
    );

    assertEquals("True/False answer must be 'True' or 'False'", exception.getMessage());
  }

  @Test
  void updateQuestioTest() {
    Question question = new Question();

    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setCorrectAnswer("FALSE");

    validator.updateQuestion(question, request);
    assertEquals("False", question.getCorrectAnswer());
  }
}