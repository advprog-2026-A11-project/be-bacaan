package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.entity.UserProgress;
import id.ac.ui.cs.advprog.yomu.event.QuizCompletionEvent;
import id.ac.ui.cs.advprog.yomu.repository.ReadingRepository;
import id.ac.ui.cs.advprog.yomu.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuizService {

  private final ReadingRepository readingRepository;
  private final UserProgressRepository userProgressRepository;
  private final ApplicationEventPublisher eventPublisher;

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
    validateId(userId);
    validateId(readingId);

    if (userProgressRepository.existsByUserIdAndReadingId(userId, readingId)) {
      throw new IllegalStateException("Congratulations! You've completed this quiz!");
    }

    return readingRepository.findById(readingId)
        .orElseThrow(() -> new IllegalArgumentException("Reading not found"));
  }

  @Transactional
  public void completeQuiz(String userId, String readingId) {
    String cleanUserId = validateId(userId);
    String cleanReadingId = validateId(readingId);

    if (userProgressRepository.existsByUserIdAndReadingId(cleanUserId, cleanReadingId)) {
      throw new IllegalStateException("This quiz has been completed");
    }

    // Simpan user progress
    UserProgress progress = new UserProgress();
    progress.setUserId(userId);
    progress.setReadingId(readingId);
    progress.setCompletedAt(LocalDateTime.now());
    userProgressRepository.save(progress);

    // Publish event dengan data yang sudah divalidasi
    eventPublisher.publishEvent(
        new QuizCompletionEvent(this, progress.getUserId(), progress.getReadingId()));
  }
}