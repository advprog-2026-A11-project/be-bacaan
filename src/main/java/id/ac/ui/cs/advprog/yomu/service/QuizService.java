package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.dto.QuizCompletedEvent;
import id.ac.ui.cs.advprog.yomu.dto.QuizResultResponse;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.entity.UserProgress;
import id.ac.ui.cs.advprog.yomu.event.QuizCompletionEvent;
import id.ac.ui.cs.advprog.yomu.repository.QuizRepository;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

  private final ReadingRepository readingRepository;
  private final UserProgressRepository userProgressRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final RestTemplate restTemplate;
  private final QuizRepository quizRepository;

  @Value("${achievement.service.url:http://be-achievement:8081}")
  private String achievementServiceUrl;

  private String validateId(String id) {
    if (id == null) {
      throw new IllegalArgumentException("Invalid ID format");
    }
    String trimmed = id.trim();
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
    completeQuiz(userId, readingId, score, accuracy, Map.of());
  }

  @Transactional
  public void completeQuiz(String userId, String readingId, int score, double accuracy,
                           Map<String, String> userAnswers) {
    String cleanUserId = validateId(userId);
    String cleanReadingId = validateId(readingId);

    if (userProgressRepository.existsByUserIdAndReadingId(cleanUserId, cleanReadingId)) {
      throw new IllegalStateException("This quiz has been completed");
    }

    UserProgress progress = new UserProgress();
    progress.setUserId(cleanUserId);
    progress.setReadingId(cleanReadingId);
    progress.setCompletedAt(LocalDateTime.now());
    progress.setScore(score);
    progress.setAccuracy(accuracy);
    progress.setUserAnswers(userAnswers != null ? userAnswers : Map.of());

    userProgressRepository.save(progress);

    eventPublisher.publishEvent(
        new QuizCompletionEvent(this, progress.getUserId(), progress.getReadingId()));

    notifyAchievementService(cleanUserId, score, accuracy);
  }

  public QuizResultResponse getQuizResult(String userId, String readingId) {
    String cleanUserId = validateId(userId);
    String cleanReadingId = validateId(readingId);

    UserProgress progress = userProgressRepository
        .findByUserIdAndReadingId(cleanUserId, cleanReadingId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Quiz result not found. User has not completed this quiz."));

    List<Question> questions = quizRepository.findByReadingId(cleanReadingId);
    Map<String, String> userAnswers = progress.getUserAnswers();

    List<QuizResultResponse.QuestionResultDetail> details = questions.stream()
        .map(question -> {
          String userAnswer = userAnswers.getOrDefault(question.getId(), null);

          String rawCorrectAnswer = question.getCorrectAnswer();
          String resolvedCorrectAnswer = resolveCorrectAnswerText(question, rawCorrectAnswer);

          boolean isCorrect = isAnswerCorrect(userAnswer, resolvedCorrectAnswer, question);

          return QuizResultResponse.QuestionResultDetail.builder()
              .questionId(question.getId())
              .questionText(question.getText())
              .questionType(question.getQuestionType())
              .options(question.getOptions())
              .userAnswer(userAnswer)
              .correctAnswer(resolvedCorrectAnswer) // Frontend akan menerima teks opsi utuh
              .isCorrect(isCorrect)
              .build();
        })
        .collect(Collectors.toList());

    return QuizResultResponse.builder()
        .readingId(cleanReadingId)
        .score(progress.getScore())
        .accuracy(progress.getAccuracy())
        .totalQuestions(questions.size())
        .correctAnswers((int) details.stream().filter(QuizResultResponse.QuestionResultDetail::isCorrect).count())
        .completedAt(progress.getCompletedAt())
        .questionDetails(details)
        .build();
  }

  private boolean isAnswerCorrect(String userAnswer, String correctAnswer) {
    if (userAnswer == null || correctAnswer == null) {
      return false;
    }
    return userAnswer.trim().toUpperCase().equals(correctAnswer.trim().toUpperCase());
  }

  public boolean isAnswerCorrect(String userAnswer, String correctAnswer, Question question) {
    if (userAnswer == null || correctAnswer == null) {
      return false;
    }

    if ("MULTIPLE_CHOICE".equalsIgnoreCase(question.getQuestionType()) && correctAnswer.trim().length() == 1) {
      String resolvedCorrect = resolveCorrectAnswerText(question, correctAnswer);
      return userAnswer.trim().equalsIgnoreCase(resolvedCorrect.trim());
    }

    return userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
  }

  private void notifyAchievementService(String userId, int score, double accuracy) {
    try {
      String url = achievementServiceUrl + "/api/events/quiz-completed";
      QuizCompletedEvent event = new QuizCompletedEvent(userId, score, accuracy);
      restTemplate.postForObject(url, event, String.class);
      log.info("Successfully notified be-achievement for user {}", userId);
    } catch (Exception e) {
      log.error("Failed to notify be-achievement service for user {}: {}", userId, e.getMessage());
    }
  }

  private String resolveCorrectAnswerText(Question question, String correctAnswer) {
    if (question.getOptions() == null || question.getOptions().isEmpty()) {
      return correctAnswer;
    }

    String cleanAnswer = correctAnswer.trim().toUpperCase();
    if (cleanAnswer.matches("[A-Z]")) {
      int index = cleanAnswer.charAt(0) - 'A';
      if (index >= 0 && index < question.getOptions().size()) {
        return question.getOptions().get(index);
      }
    }

    return correctAnswer;
  }
}