package id.ac.ui.cs.advprog.yomu.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CompletedQuizRequest {
  private int score;
  private double accuracy;

}
