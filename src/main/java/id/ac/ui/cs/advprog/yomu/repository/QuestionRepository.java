package id.ac.ui.cs.advprog.yomu.repository;

import id.ac.ui.cs.advprog.yomu.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {
  List<Question> findByReadingId(String id);
}
