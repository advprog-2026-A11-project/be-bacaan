package id.ac.ui.cs.advprog.yomu.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class QuizValidatorFactory {

  private final Map<String, QuizValidator> validators;

  public QuizValidatorFactory(List<QuizValidator> validatorList) {
    this.validators = new ConcurrentHashMap<>();
    for (QuizValidator validator : validatorList) {
      validators.put(validator.getQuestionType(), validator);
    }
  }

  public QuizValidator getValidator(String quizType) {
    QuizValidator validator = validators.get(quizType);
    if (validator == null) {
      throw new IllegalArgumentException(
          "Invalid quiz type" + quizType + ". Must be MULTIPLE_CHOICE, TRUE_FALSE, ESSAY"
      );
    }

    return validator;
  }
}
