package id.ac.ui.cs.advprog.yomu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

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

  // pilihan ganda
  @ElementCollection
  @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
  @Column(name = "option_text", nullable = false)
  private List<String> options = new ArrayList<>();

  @Column(nullable = false)
  private String correctAnswer;

  // untuk essay, multiple choice, true-false
  @Column(nullable = false)
  private String questionType = "MULTIPLE_CHOICE";

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reading_id", nullable = false)
  @JsonIgnore
  private Reading reading;
}