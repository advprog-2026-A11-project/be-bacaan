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

import  java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuizService {
  private final ReadingRepository readingRepository;
  private final UserProgressRepository userProgressRepository;
  private final ApplicationEventPublisher eventPublisher;

  public Reading getReading(String userId, String readingId) {
    if(userProgressRepository.existsByUserIdAndReadingId(userId, readingId)) {
      throw new IllegalStateException("Congratulations! You've completed this quiz!");
    }
    return readingRepository.findById(readingId)
        .orElseThrow(() -> new IllegalArgumentException("Not found"));
  }

  @Transactional
  public void completeQuiz(String userId, String readingId) {
    if(userProgressRepository.existsByUserIdAndReadingId(userId, readingId)) {
      throw new IllegalStateException("This quiz has been completed");
    }

    // menyimpan user progress
    UserProgress progress = new UserProgress();
    progress.setUserId(userId);
    progress.setReadingId(readingId);
    progress.setCompletedAt(LocalDateTime.now());
    userProgressRepository.save(progress);

    // trigger untuk modul lain
    eventPublisher.publishEvent(new QuizCompletionEvent(this, userId, readingId));
  }
}
