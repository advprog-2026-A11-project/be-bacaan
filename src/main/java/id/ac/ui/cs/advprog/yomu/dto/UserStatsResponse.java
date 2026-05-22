package id.ac.ui.cs.advprog.yomu.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserStatsResponse {
  private String userId;
  private long totalCompleted;
  private long completionFrequency;
  private double averageAccuracy;
}
