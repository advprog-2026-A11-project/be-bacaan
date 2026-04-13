package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.dto.QuizCompletedEvent;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.entity.UserProgress;
import id.ac.ui.cs.advprog.yomu.event.QuizCompletionEvent;
import id.ac.ui.cs.advprog.yomu.repository.ReadingRepository;
import id.ac.ui.cs.advprog.yomu.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

  private final ReadingRepository readingRepository;
  private final UserProgressRepository userProgressRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final RestTemplate restTemplate;

  @Value("${achievement.service.url:http://be-achievement:8081}")
  private String achievementServiceUrl;

  private String validateId(String id) {
    if (id == null) {
      throw new IllegalArgumentException("Invalid ID format");
    }
    String trimmed = id.trim(); // trim dulu
    if (!trimmed.matches("[a-zA-Z0-9\\-]+")) {
      throw new IllegalArgumentException("Invalid ID format");
    }
    return trimmed;
  }

  public Reading getReading(String userId, String readingId) {
    String cleanUserId = validateId(userId);
    String cleanReadingId = validateId(readingId);

    if (userProgressRepository.existsByUserIdAndReadingId(cleanUserId, cleanReadingId)) {
      throw new IllegalStateException("Congratulations! You've completed this quiz!");
    }

    return readingRepository.findById(cleanReadingId)
        .orElseThrow(() -> new IllegalArgumentException("Reading not found"));
  }

  @Transactional
  public void completeQuiz(String userId, String readingId, int score, double accuracy) {
    String cleanUserId = validateId(userId);
    String cleanReadingId = validateId(readingId);

    if (userProgressRepository.existsByUserIdAndReadingId(cleanUserId, cleanReadingId)) {
      throw new IllegalStateException("This quiz has been completed");
    }

    // Simpan user progress
    UserProgress progress = new UserProgress();
    progress.setUserId(cleanUserId);
    progress.setReadingId(cleanReadingId);
    progress.setCompletedAt(LocalDateTime.now());
    progress.setScore(score);
    progress.setAccuracy(accuracy);

    userProgressRepository.save(progress);

    // Publish event dengan data yang sudah divalidasi
    eventPublisher.publishEvent(
        new QuizCompletionEvent(this, progress.getUserId(), progress.getReadingId()));

    // Kirim event ke be-achievement lewat HTTP POST
    notifyAchievementService(cleanUserId, score, accuracy);
  }

  private void notifyAchievementService(String userId, int score, double accuracy) {
    try {
      String url = achievementServiceUrl + "/api/events/quiz-completed";
      QuizCompletedEvent event = new QuizCompletedEvent(userId, score, accuracy);
      restTemplate.postForObject(url, event, String.class);
      log.info("Successfully notified be-achievement for user {}", userId);
    } catch (Exception e) {
      // Jangan gagalkan transaksi utama jika be-achievement tidak tersedia
      log.error("Failed to notify be-achievement service for user {}: {}", userId, e.getMessage());
    }
  }
}