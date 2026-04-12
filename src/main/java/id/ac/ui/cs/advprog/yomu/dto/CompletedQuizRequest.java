package id.ac.ui.cs.advprog.yomu.dto;

public class CompletedQuizRequest {
  private int score;
  private double accuracy;

  public int getScore() { return score;}

  public double getAccuracy() { return accuracy;}

  public void setScore(int score) { this.score = score;}

  public void setAccuracy(double accuracy) { this.accuracy = accuracy;}
}
