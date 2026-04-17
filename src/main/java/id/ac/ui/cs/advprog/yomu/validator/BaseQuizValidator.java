package id.ac.ui.cs.advprog.yomu.validator;

import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionRequest;

public class BaseQuizValidator {

  protected static final int MIN_QUESTION_LENGTH = 5;
  protected static final int MAX_QUESTION_LENGTH = 500;

  protected void validateQuestionText(String text) {
    if (text == null || text.trim().isEmpty()) {
      throw new IllegalArgumentException("Question text cannot be empty");
    }
    if (text.length() < MIN_QUESTION_LENGTH) {
      throw new IllegalArgumentException(
          "Question text must be at least " + MIN_QUESTION_LENGTH + " characters");
    }
    if (text.length() > MAX_QUESTION_LENGTH) {
      throw new IllegalArgumentException(
          "Question text cannot exceed " + MAX_QUESTION_LENGTH + " characters");
    }
  }

  protected void validateCommon(QuizQuestionRequest request) {
    validateQuestionText(request.getText());
    if (request.getQuestionType() == null ||
        request.getQuestionType().isEmpty()) {
      throw new IllegalArgumentException("Quiz type cannot be empty");
    }
  }
}
