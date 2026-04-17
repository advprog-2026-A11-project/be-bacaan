package id.ac.ui.cs.advprog.yomu.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizQuestionResponse {
  private String id;
  private String text;
}
