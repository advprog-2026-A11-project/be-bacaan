package id.ac.ui.cs.advprog.yomu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreUpdateRequest {
  private String userId;
  private int score;
  private int accuracy;

  @JsonProperty("isQuiz")
  private boolean isQuiz;
}
