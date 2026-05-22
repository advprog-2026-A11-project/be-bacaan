package id.ac.ui.cs.advprog.yomu.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class QuizCompletedEvent {
  private String eventId;
  private String userId;
  private String readingId;
  private String category;
  private String difficultyLevel;
  private int score;
  private int accuracy;

  public QuizCompletedEvent() {}

  public QuizCompletedEvent(String userId, String readingId, String category,
                            String difficultyLevel, int score, int accuracy) {
    this.eventId = UUID.randomUUID().toString();
    this.userId = userId;
    this.readingId = readingId;
    this.category = category;
    this.difficultyLevel = difficultyLevel;
    this.score = score;
    this.accuracy = accuracy;
  }

}
