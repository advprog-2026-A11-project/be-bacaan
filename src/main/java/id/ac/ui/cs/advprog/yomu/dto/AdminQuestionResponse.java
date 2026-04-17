package id.ac.ui.cs.advprog.yomu.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminQuestionResponse {
  private String id;
  private String text;
  private List<String> options;
  private String correctAnswer;
  private String questionType;
}
