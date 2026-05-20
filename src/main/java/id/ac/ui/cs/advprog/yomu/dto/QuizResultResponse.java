package id.ac.ui.cs.advprog.yomu.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class QuizResultResponse {

  private String readingId;
  private int score;
  private double accuracy;
  private int totalQuestions;
  private int correctAnswers;
  private LocalDateTime completedAt;
  private List<QuestionResultDetail> questionDetails;

  @Getter
  @Setter
  @Builder
  public static class QuestionResultDetail {
    private String questionId;
    private String questionText;
    private String questionType;
    private List<String> options;
    private String userAnswer;
    private String correctAnswer;
    private boolean isCorrect;
  }
}