package id.ac.ui.cs.advprog.yomu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

  public TextContext() {
  }

  public TextContext(String title, String content, String category) {
    this.title = title;
    this.content = content;
    this.category = category;
  }
}
