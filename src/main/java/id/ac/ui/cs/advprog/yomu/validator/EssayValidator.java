package id.ac.ui.cs.advprog.yomu.validator;

import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class EssayValidator implements QuizValidator {
  private static final int MAX_ANSWER_LENGTH = 1000;

  @Override
  public String getQuestionType() {
    return "ESSAY";
  }

  @Override
  public void validate(QuizQuestionRequest request) {
    if (request.getCorrectAnswer() == null
        || request.getCorrectAnswer().trim().isEmpty()) {
      throw new IllegalArgumentException(
          "Sample answer cannot be empty for essay question");
    }
    if (request.getCorrectAnswer().length() > MAX_ANSWER_LENGTH) {
      throw new IllegalArgumentException(
          "Sample answer cannot exceed " + MAX_ANSWER_LENGTH + " characters");
    }
    if (request.getOptions() != null
        && !request.getOptions().isEmpty()) {
      throw new IllegalArgumentException(
          "Essay questions should not have options");
    }
  }

  @Override
  public Question createQuestion(QuizQuestionRequest request, Reading reading) {
    Question question = new Question();
    question.setText(request.getText());
    question.setQuestionType(getQuestionType());
    question.setOptions(List.of());
    question.setCorrectAnswer(request.getCorrectAnswer().trim());
    question.setReading(reading);
    return question;
  }

  @Override
  public void updateQuestion(Question question, QuizQuestionRequest request) {
    if (request.getCorrectAnswer() != null) {
      if (request.getCorrectAnswer().length() > MAX_ANSWER_LENGTH) {
        throw new IllegalArgumentException(
            "Sample answer cannot exceed " + MAX_ANSWER_LENGTH + " characters");
      }
      question.setCorrectAnswer(request.getCorrectAnswer().trim());
    }
  }
}
