package id.ac.ui.cs.advprog.yomu.validator;

import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MultipleChoiceValidatorTest {

    private MultipleChoiceValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MultipleChoiceValidator();
    }

    @Test
    void getQuestionTypeTest() {
        assertEquals("MULTIPLE_CHOICE", validator.getQuestionType());
    }

    @Test
    void validateValidRequestTest() {
        QuizQuestionRequest request = new QuizQuestionRequest();
        request.setOptions(List.of("A", "B", "C"));
        request.setCorrectAnswer("A");

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void validateNullOptionsTest() {
        QuizQuestionRequest request = new QuizQuestionRequest();
        request.setOptions(null);
        request.setCorrectAnswer("A");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );

        assertEquals(
            "Options cannot be null for multiple choice question",
            exception.getMessage()
        );
    }

    @Test
    void validateLessThanMinimumOptions() {
        QuizQuestionRequest request = new QuizQuestionRequest();
        request.setOptions(List.of("Only One"));
        request.setCorrectAnswer("A");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );

        assertEquals(
            "Multiple choice questions must have at least 2 options",
            exception.getMessage()
        );
    }

    @Test
    void validateMoreThanMaximumOptions() {
        QuizQuestionRequest request = new QuizQuestionRequest();
        request.setOptions(List.of("A", "B", "C", "D", "E", "F", "G"));
        request.setCorrectAnswer("A");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );

        assertEquals(
            "Multiple choice questions cannot have more than 6 options",
            exception.getMessage()
        );
    }

    @Test
    void validateEmptyOptionTest() {
        QuizQuestionRequest request = new QuizQuestionRequest();
        request.setOptions(List.of("Option 1", ""));
        request.setCorrectAnswer("A");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );

        assertEquals(
            "Option 2 cannot be empty",
            exception.getMessage()
        );
    }

    @Test
    void validateBlankOptionTest() {
        QuizQuestionRequest request = new QuizQuestionRequest();
        request.setOptions(List.of("Option 1", "   "));
        request.setCorrectAnswer("A");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );

        assertEquals(
            "Option 2 cannot be empty",
            exception.getMessage()
        );
    }

    @Test
    void validateOptionTooLong() {
        String longOption = "a".repeat(201);

        QuizQuestionRequest request = new QuizQuestionRequest();
        request.setOptions(List.of("Valid", longOption));
        request.setCorrectAnswer("A");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );

        assertEquals(
            "Option 2 cannot exceed 200 characters",
            exception.getMessage()
        );
    }

    @Test
    void validateNullCorrectAnswer() {
        QuizQuestionRequest request = new QuizQuestionRequest();
        request.setOptions(List.of("A", "B"));
        request.setCorrectAnswer(null);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );

        assertEquals(
            "Correct answer cannot be empty for multiple choice question",
            exception.getMessage()
        );
    }

    @Test
    void validateInvalidCorrectAnswer() {
        QuizQuestionRequest request = new QuizQuestionRequest();
        request.setOptions(List.of("A", "B"));
        request.setCorrectAnswer("D");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );

        assertEquals(
            "Correct answer must be one of: A, B (0-1)",
            exception.getMessage()
        );
    }

    @Test
    void validateNumericCorrectAnswer() {
        QuizQuestionRequest request = new QuizQuestionRequest();
        request.setOptions(List.of("Option A", "Option B", "Option C"));
        request.setCorrectAnswer("1");

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void createQuestionTest() {
        QuizQuestionRequest request = new QuizQuestionRequest();
        request.setText("What is Java?");
        request.setOptions(List.of("Language", "Database"));
        request.setCorrectAnswer("0");

        Reading reading = new Reading();

        Question question = validator.createQuestion(request, reading);

        assertEquals("What is Java?", question.getText());
        assertEquals("MULTIPLE_CHOICE", question.getQuestionType());
        assertEquals(List.of("Language", "Database"), question.getOptions());
        assertEquals("A", question.getCorrectAnswer());
        assertEquals(reading, question.getReading());
    }

    

    @Test
    void updateQuestionVlidOptions() {
        Question question = new Question();
        question.setOptions(List.of("Old A", "Old B"));

        QuizQuestionRequest request = new QuizQuestionRequest();
        request.setOptions(List.of("New A", "New B", "New C"));

        validator.updateQuestion(question, request);

        assertEquals(
                List.of("New A", "New B", "New C"),
                question.getOptions()
        );
    }

    @Test
    void updateQuestionValidCorrectAnswer() {
        Question question = new Question();
        question.setOptions(List.of("A", "B", "C"));
        question.setCorrectAnswer("A");

        QuizQuestionRequest request = new QuizQuestionRequest();
        request.setCorrectAnswer("2");

        validator.updateQuestion(question, request);

        assertEquals("C", question.getCorrectAnswer());
    }

    @Test
    void updateQuestionInvalidCorrectAnswer() {
        Question question = new Question();
        question.setOptions(List.of("A", "B"));

        QuizQuestionRequest request = new QuizQuestionRequest();
        request.setCorrectAnswer("F");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.updateQuestion(question, request)
        );

        assertEquals(
            "Correct answer must be one of: A, B (0-1)",
            exception.getMessage()
        );
    }

    @Test
    void updateQuestionNullCorrectAnswer() {
        Question question = new Question();
        question.setOptions(List.of("A", "B"));
        question.setCorrectAnswer("A");

        QuizQuestionRequest request = new QuizQuestionRequest();
        request.setCorrectAnswer(null);

        validator.updateQuestion(question, request);

        assertEquals("A", question.getCorrectAnswer());
    }

    @Test
    void updateQuestionEmptyOptionsTest() {
        Question question = new Question();
        question.setOptions(List.of("A", "B"));

        QuizQuestionRequest request = new QuizQuestionRequest();
        request.setOptions(List.of());

        validator.updateQuestion(question, request);

        assertEquals(List.of("A", "B"), question.getOptions());
    }
}