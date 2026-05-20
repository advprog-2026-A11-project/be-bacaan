package id.ac.ui.cs.advprog.yomu.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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

  @ElementCollection
  @CollectionTable(
      name = "user_progress_answers",
      joinColumns = @JoinColumn(name = "user_progress_id")
  )
  @MapKeyColumn(name = "question_id")
  @Column(name = "user_answer")
  private Map<String, String> userAnswers = new HashMap<>();
}