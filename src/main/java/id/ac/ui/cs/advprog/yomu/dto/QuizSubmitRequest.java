package id.ac.ui.cs.advprog.yomu.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.*;
import java.util.Map;

@Getter
@Setter
public class QuizSubmitRequest {
  @NotNull(message = "Answers cannot be null")
  @Size(min = 1, message = "At least one answer required")
  private Map<String, String> answers; // key: questionId, value: userAnswer

  @Min(value = 0, message = "Time taken must be positive")
  private int timeTakenSeconds;
}