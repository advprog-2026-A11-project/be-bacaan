package id.ac.ui.cs.advprog.yomu.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReadingResponse {
  private String id;
  private String title;
  private String content;
  private String category;
  private String difficultyLevel;
}
