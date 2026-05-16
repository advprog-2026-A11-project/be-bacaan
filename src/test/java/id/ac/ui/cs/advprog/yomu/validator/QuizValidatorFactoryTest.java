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

        when(multipleChoiceValidator.getQuestionType()).thenReturn("MULTIPLE_CHOICE");

        when(essayValidator.getQuestionType()).thenReturn("ESSAY");

        factory = new QuizValidatorFactory(List.of(multipleChoiceValidator, essayValidator));
    }

    @Test
    void getValidator_whenValidMultipleChoice_shouldReturnValidator() {
        QuizValidator result = factory.getValidator("MULTIPLE_CHOICE");

        assertNotNull(result);
        assertEquals(multipleChoiceValidator, result);
    }

    @Test
    void getValidator_whenValidEssay_shouldReturnValidator() {
        QuizValidator result = factory.getValidator("ESSAY");

        assertNotNull(result);
        assertEquals(essayValidator, result);
    }

    @Test
    void getValidator_whenInvalidQuizType_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> factory.getValidator("INVALID"));

        assertEquals(
            "Invalid quiz typeINVALID. Must be MULTIPLE_CHOICE, TRUE_FALSE, ESSAY",
            exception.getMessage());
    }

    @Test
    void constructor_shouldStoreAllValidators() {
        QuizValidator result1 = factory.getValidator("MULTIPLE_CHOICE");
        QuizValidator result2 = factory.getValidator("ESSAY");

        assertEquals(multipleChoiceValidator, result1);
        assertEquals(essayValidator, result2);
    }
}