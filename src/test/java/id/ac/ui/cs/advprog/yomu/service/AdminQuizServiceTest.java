package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.repository.QuizRepository;
import id.ac.ui.cs.advprog.yomu.repository.ReadingRepository;
import id.ac.ui.cs.advprog.yomu.validator.QuizValidator;
import id.ac.ui.cs.advprog.yomu.validator.QuizValidatorFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminQuizServiceTest {

  @Mock private QuizRepository questionRepository;
  @Mock private ReadingRepository readingRepository;
  @Mock private QuizValidatorFactory validatorFactory;
  @Mock private QuizValidator validator;

  @InjectMocks
  private AdminQuizService adminQuizService;

  @Test
  void testAddQuestionShouldUseCorrectValidator() {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setQuestionType("MULTIPLE_CHOICE");
    request.setText("Test question?");
    request.setOptions(List.of("A", "B", "C"));
    request.setCorrectAnswer("A");

    String readingId = "reading123";
    Reading reading = new Reading();
    reading.setId(readingId);

    when(readingRepository.existsById(readingId)).thenReturn(true);
    when(readingRepository.findById(readingId)).thenReturn(Optional.of(reading));
    when(validatorFactory.getValidator("MULTIPLE_CHOICE")).thenReturn(validator);

    Question expectedQuestion = new Question();
    when(validator.createQuestion(eq(request), any(Reading.class))).thenReturn(expectedQuestion);
    when(questionRepository.save(any(Question.class))).thenReturn(expectedQuestion);

    Question result = adminQuizService.addQuestion(readingId, request);

    // Assert
    assertThat(result).isEqualTo(expectedQuestion);
    verify(validator).validate(request);
    verify(validator).createQuestion(eq(request), any(Reading.class));
  }

  @Test
  void testAddQuestWithInvalidQuestType() {
    String readingId = "reading123";
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setQuestionType("INVALID_TYPE");

    when(readingRepository.existsById(readingId)).thenReturn(true);
    when(validatorFactory.getValidator("INVALID_TYPE"))
        .thenThrow(new IllegalArgumentException("Invalid question type"));

    assertThatThrownBy(() -> adminQuizService.addQuestion(readingId, request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid question type");
  }
}
