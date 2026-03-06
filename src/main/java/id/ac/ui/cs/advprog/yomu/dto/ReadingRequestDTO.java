package id.ac.ui.cs.advprog.yomu.dto;

import lombok.Data;

@Data
public class ReadingRequestDTO {
  private String title;
  private String content;
  private String category;
  private String difficultyLevel;
}