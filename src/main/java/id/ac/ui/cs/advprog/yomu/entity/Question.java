package id.ac.ui.cs.advprog.yomu.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String text;

  @Column(nullable = false)
  private String correctAnswer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reading_id", nullable = false)
  private Reading reading;
}