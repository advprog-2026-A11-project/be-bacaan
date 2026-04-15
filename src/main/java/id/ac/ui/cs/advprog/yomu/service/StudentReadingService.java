package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.entity.UserProgress;
import id.ac.ui.cs.advprog.yomu.repository.ReadingRepository;
import id.ac.ui.cs.advprog.yomu.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentReadingService {

  private final ReadingRepository readingRepository;
  private final UserProgressRepository userProgressRepository;

  public Reading getReading(String userId, String readingId) {
    return readingRepository.findById(readingId)
        .orElseThrow(() -> new IllegalArgumentException("Reading not found"));
  }

  public List<Reading> getAllReadings() {
    return readingRepository.findAll();
  }

  public Map<String, Object> getUserStats(String userId) {
    List<UserProgress> progresses = userProgressRepository.findByUserId(userId);

    long totalCompleted = progresses.size();
    double avgAccuracy = progresses.stream()
        .mapToDouble(UserProgress::getAccuracy)
        .average()
        .orElse(0.0);

    Map<String, Object> stats = new HashMap<>();
    stats.put("userId", userId);
    stats.put("totalCompleted", totalCompleted);
    stats.put("completionFrequency", totalCompleted);
    stats.put("averageAccuracy", avgAccuracy);

    return stats;
  }
}
