package id.ac.ui.cs.advprog.yomu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_progress")
@Getter
@Setter
public class UserProgress {

  @Id
  private String id = UUID.randomUUID().toString();

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private String readingId;

  @Column(nullable = false)
  private LocalDateTime completedAt;

  @Column(nullable = false)
  private int score;

  @Column(nullable = false)
  private double accuracy;
}