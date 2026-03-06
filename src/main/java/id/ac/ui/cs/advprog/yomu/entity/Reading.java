package id.ac.ui.cs.advprog.yomu.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String difficultyLevel;

    // Relasi ke Question
    @OneToMany(mappedBy = "reading", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();
}