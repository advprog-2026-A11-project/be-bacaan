package id.ac.ui.cs.advprog.yomu.validator;

import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MultipleChoiceValidator implements QuizValidator {

  private static final int MIN_OPTIONS = 2;
  private static final int MAX_OPTIONS = 6;
  private static final int MAX_OPTION_LENGTH = 200;

  @Override
  public String getQuestionType() {
    return "MULTIPLE_CHOICE";
  }

  @Override
  public void validate(QuizQuestionRequest request) {
    validateOptions(request.getOptions());
    validateCorrectAnswer(request);
  }

  @Override
  public Question createQuestion(QuizQuestionRequest request, Reading reading) {
    Question question = new Question();
    question.setText(request.getText());
    question.setQuestionType(getQuestionType());
    question.setOptions(request.getOptions());
    question.setCorrectAnswer(normalizeAnswer(request.getCorrectAnswer()));
    question.setReading(reading);
    return question;
  }

  @Override
  public void updateQuestion(Question question, QuizQuestionRequest request) {
    if (request.getOptions() != null && !request.getOptions().isEmpty()) {
      validateOptions(request.getOptions());
      question.setOptions(request.getOptions());
    }

    if (request.getCorrectAnswer() != null) {
      String normalized = normalizeAnswer(request.getCorrectAnswer());
      validateAnswerIndex(normalized, question.getOptions().size());
      question.setCorrectAnswer(normalized);
    }
  }

  private void validateOptions(List<String> options) {
    if (options == null) {
      throw new IllegalArgumentException(
          "Options cannot be null for multiple choice question");
    }

    if (options.size() < MIN_OPTIONS) {
      throw new IllegalArgumentException(
          "Multiple choice questions must have at least " + MIN_OPTIONS + " options");
    }

    if (options.size() > MAX_OPTIONS) {
      throw new IllegalArgumentException(
          "Multiple choice questions cannot have more than " + MAX_OPTIONS + " options");
    }

    for (int i = 0; i < options.size(); i++) {
      String option = options.get(i);
      if (option == null || option.trim().isEmpty()) {
        throw new IllegalArgumentException("Option " + (i + 1) + " cannot be empty");
      }

      if (option.length() > MAX_OPTION_LENGTH) {
        throw new IllegalArgumentException(
            "Option " + (i + 1) + " cannot exceed " + MAX_OPTION_LENGTH + " characters");
      }
    }
  }

  private void validateCorrectAnswer(QuizQuestionRequest request) {
    if (request.getCorrectAnswer() == null || request.getCorrectAnswer().trim().isEmpty()) {
      throw new IllegalArgumentException(
          "Correct answer cannot be empty for multiple choice question");
    }

    String normalizedAnswer = normalizeAnswer(request.getCorrectAnswer());
    validateAnswerIndex(normalizedAnswer, request.getOptions().size());
  }

  private String normalizeAnswer(String answer) {
    if (answer == null) {
      return null;
    }

    String trimmed = answer.trim().toUpperCase();

    if (trimmed.length() == 1 && trimmed.charAt(0) >= 'A' &&
        trimmed.charAt(0) <= 'F') {
      return trimmed;
    }

    try {
      int index = Integer.parseInt(trimmed);
      if (index >= 0 && index <= 5) {
        return String.valueOf((char) ('A' + index));
      }
    } catch (NumberFormatException e) {
      // Bukan angka
    }
    return trimmed;
  }

  private void validateAnswerIndex(String normalizedAnswer, int optionCount) {
    int index = getAnswerIndex(normalizedAnswer);
    if (index < 0 || index >= optionCount) {
      throw new IllegalArgumentException(
          String.format("Correct answer must be one of: %s (0-%d)",
              getOptionLetters(optionCount), optionCount - 1)
      );
    }
  }

  private int getAnswerIndex(String normalizedAnswer) {
    if (normalizedAnswer == null || normalizedAnswer.isEmpty()) {
      return -1;
    }

    if (normalizedAnswer.length() == 1 &&
        normalizedAnswer.charAt(0) >= 'A' && normalizedAnswer.charAt(0) <= 'F') {
      return normalizedAnswer.charAt(0) - 'A';
    }

    try {
      return Integer.parseInt(normalizedAnswer);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private String getOptionLetters(int count) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
      if (i > 0) {
        sb.append(", ");
      }

      sb.append((char) ('A' + i));
    }
    return sb.toString();
  }
}