package id.ac.ui.cs.advprog.yomu.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class QuizCompletedEvent {
  private String userId;
  private int score;
  private double accuracy;

  public QuizCompletedEvent() {}

  public QuizCompletedEvent(String userId, int score, double accuracy) {
    this.userId = userId;
    this.score = score;
    this.accuracy = accuracy;
  }

}
