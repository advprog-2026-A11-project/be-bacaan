package id.ac.ui.cs.advprog.yomu.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "text_context")
@Getter
@Setter
public class TextContext {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  private String category;
  private String difficultyLevel;

  public TextContext() {
  }

  public TextContext(String title, String content, String category, String difficultyLevel) {
    this.title = title;
    this.content = content;
    this.category = category;
    this.difficultyLevel = difficultyLevel;
  }
}
