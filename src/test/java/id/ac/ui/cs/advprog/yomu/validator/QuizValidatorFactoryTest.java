package id.ac.ui.cs.advprog.yomu.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuizValidatorFactoryTest {

  private QuizValidator multipleChoiceValidator;
  private QuizValidator essayValidator;
  private QuizValidatorFactory factory;

  @BeforeEach
  void setUp() {

    multipleChoiceValidator = mock(QuizValidator.class);
    essayValidator = mock(QuizValidator.class);

    when(multipleChoiceValidator.getQuestionType())
        .thenReturn("MULTIPLE_CHOICE");

    when(essayValidator.getQuestionType())
        .thenReturn("ESSAY");

    factory = new QuizValidatorFactory(
        List.of(multipleChoiceValidator, essayValidator)
    );
  }

  @Test
  void getValidator_shouldReturnMultipleChoiceValidator() {

    QuizValidator result =
        factory.getValidator("MULTIPLE_CHOICE");

    assertNotNull(result);
    assertSame(multipleChoiceValidator, result);
  }

  @Test
  void getValidator_shouldReturnEssayValidator() {

    QuizValidator result =
        factory.getValidator("ESSAY");

    assertNotNull(result);
    assertSame(essayValidator, result);
  }

  @Test
  void getValidator_shouldThrowExceptionWhenInvalidQuizType() {

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> factory.getValidator("INVALID")
    );

    assertEquals(
        "Invalid quiz typeINVALID. Must be MULTIPLE_CHOICE, TRUE_FALSE, ESSAY",
        exception.getMessage()
    );
  }

  @Test
  void constructor_shouldStoreAllValidatorsCorrectly() {

    assertSame(
        multipleChoiceValidator,
        factory.getValidator("MULTIPLE_CHOICE")
    );

    assertSame(
        essayValidator,
        factory.getValidator("ESSAY")
    );
  }
}