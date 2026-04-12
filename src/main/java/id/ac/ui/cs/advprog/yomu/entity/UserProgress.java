package id.ac.ui.cs.advprog.yomu.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class UserProgress {

  @Id
  private String id = UUID.randomUUID().toString();

  private String userId;
  private String readingId;
  private LocalDateTime completedAt;
  private int score;
  private double accuracy;
}