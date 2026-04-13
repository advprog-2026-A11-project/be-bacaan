package id.ac.ui.cs.advprog.yomu.dto;

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

  public String getUserId() { return  userId;}

  public void setUserId(String userId) { this.userId = userId;}

  public int getScore() { return score;}

  public void setScore(int score) { this.score = score;}

  public double getAccuracy() { return accuracy;}

  public void setAccuracy(double accuracy) { this.accuracy = accuracy;}
}
