package id.ac.ui.cs.advprog.yomu.repository;

import id.ac.ui.cs.advprog.yomu.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProgressRepository  extends JpaRepository<UserProgress, String> {
  boolean existsByUserIdAndReadingId(String userId, String readingId);
}
