package id.ac.ui.cs.advprog.yomu.validator;

import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TrueFalseValidator implements QuizValidator {

  @Override
  public String getQuestionType() {
    return "TRUE_FALSE";
  }

  @Override
  public void validate(QuizQuestionRequest request) {
    if (request.getCorrectAnswer() == null || request.getCorrectAnswer().trim().isEmpty()) {
      throw new IllegalArgumentException("Correct answer cannot be empty for true/false question");
    }
    validateAnswer(request.getCorrectAnswer());
  }

  @Override
  public Question createQuestion(QuizQuestionRequest request, Reading reading) {
    Question question = new Question();
    question.setText(request.getText());
    question.setQuestionType(getQuestionType());
    question.setOptions(List.of("True", "False"));
    question.setCorrectAnswer(capitalizeFirstLetter(request.getCorrectAnswer()));
    question.setReading(reading);
    return question;
  }

  @Override
  public void updateQuestion(Question question, QuizQuestionRequest request) {
    if (request.getCorrectAnswer() != null) {
      validateAnswer(request.getCorrectAnswer());
      question.setCorrectAnswer(capitalizeFirstLetter(request.getCorrectAnswer()));
    }
  }

  private void validateAnswer(String answer) {
    String normalized = answer.trim().toLowerCase();
    if (!normalized.equals("true") && !normalized.equals("false")) {
      throw new IllegalArgumentException("True/False answer must be 'True' or 'False'");
    }
  }

  private String capitalizeFirstLetter(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }

    String lower = text.trim().toLowerCase();
    return lower.substring(0, 1).toUpperCase() + lower.substring(1);
  }
}
