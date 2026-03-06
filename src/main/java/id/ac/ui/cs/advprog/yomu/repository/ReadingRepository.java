package id.ac.ui.cs.advprog.yomu.repository;

import id.ac.ui.cs.advprog.yomu.entity.Reading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

public interface ReadingRepository extends JpaRepository<Reading, String> {
  List<Reading> findByCategory(String category);

  List<Reading> findByDifficultyLevel(String diffLevel);
}
