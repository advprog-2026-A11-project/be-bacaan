package id.ac.ui.cs.advprog.yomu.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class QuizSubmitResponse {
  private int score;
  private int accuracy;
  private int totalQuestions;
  private int correctAnswers;
  private double timeTaken;
  private Map<String, Boolean> questionResults;
}
