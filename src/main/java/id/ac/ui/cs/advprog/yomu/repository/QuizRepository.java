package id.ac.ui.cs.advprog.yomu.repository;

import id.ac.ui.cs.advprog.yomu.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Question, String> {
  List<Question> findByReadingId(String id);

  @Query("SELECT q from Question q WHERE q.reading.id = :readingId")
  List<Question> findAllByReadingId(@Param("readingId") String readingId);

  long countByReadingId(String readingId);
  Optional<Question> findByIdAndByReadingId(String id, String readingId);
  void deleteByReadingId(String readingId);
}
