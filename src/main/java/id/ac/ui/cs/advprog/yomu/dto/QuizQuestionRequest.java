package id.ac.ui.cs.advprog.yomu.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizQuestionRequest {

  @NotBlank(message = "Question text cannot be empty")
  @Size(min = 5, message = "Question text must be at least 5 characters")
  @Size(max = 500, message = "Question text cannot exceed 500 characters")
  private String text;

  // pilihan ganda
  @Size(min = 2, message = "Multiple choice questions must have at least 2 options")
  @Size(max = 4, message = "Maximum 4 options allowed")
  private List<@NotBlank String> options;

  @NotBlank(message = "Correct answer cannot be empty")
  private String correctAnswer;

  @NotBlank(message = "Question type cannot be empty")
  @Pattern(regexp = "MULTIPLE_CHOICE|ESSAY|TRUE_FALSE",
      message = "Question type must be MULTIPLE_CHOICE, ESSAY, or TRUE_FALSE")
  private String questionType = "MULTIPLE_CHOICE";

}
