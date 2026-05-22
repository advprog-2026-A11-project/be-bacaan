package id.ac.ui.cs.advprog.yomu.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitRequest;
import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitResponse;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.repository.QuizRepository;
import id.ac.ui.cs.advprog.yomu.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentQuizService {
  private final QuizRepository quizRepository;
  private final UserProgressRepository userProgressRepository;
  private final QuizService quizService;

  public List<Question> getQuizQuestion(String userId, String readingId) {
    validateNotCompleted(userId, readingId);
    return quizRepository.findByReadingId(readingId);
  }

  public QuizSubmitResponse submitQuiz(String userId, String readingId,
                                       QuizSubmitRequest request) {

    validateNotCompleted(userId, readingId);
    validateRequest(request);

    List<Question> questions = quizRepository.findByReadingId(readingId);

    if (questions.isEmpty()) {
      throw new IllegalStateException("No quiz available for this reading");
    }

    Map<String, String> studentAnswers = request.getAnswers();

    int correctCount = 0;
    Map<String, Boolean> questionResults = new HashMap<>();

    for (Question question : questions) {
      String questId = question.getId();
      String studentAnswer = studentAnswers.get(questId);
      String correctAnswer = question.getCorrectAnswer();

      boolean isCorrect = quizService.isAnswerCorrect(studentAnswer, correctAnswer, question);

      questionResults.put(questId, isCorrect);

      if (isCorrect) {
        correctCount++;
      }
    }

    int totalQuestions = questions.size();
    int score = (int) Math.round((double) correctCount / totalQuestions * 100);
    int accuracy = (int) Math.round((double) correctCount / totalQuestions * 100);

    quizService.completeQuiz(userId, readingId, score, accuracy, studentAnswers);

    return QuizSubmitResponse.builder()
        .score(score)
        .accuracy(accuracy)
        .totalQuestions(totalQuestions)
        .correctAnswers(correctCount)
        .timeTaken(request.getTimeTakenSeconds())
        .questionResults(questionResults)
        .build();
  }

  // private helper method
  private void validateNotCompleted(String userId, String readingId) {
    if (userProgressRepository.existsByUserIdAndReadingId(userId, readingId)) {
      throw new IllegalStateException("You've completed this quiz");
    }
  }

  private void validateRequest(QuizSubmitRequest request) {
    if (request == null || request.getAnswers() == null) {
      throw new IllegalStateException("Please choose the answers before submit the quiz");
    }
  }

  private boolean isAnswerCorrect(String studentAnswer, String correctAnswer) {
    if (studentAnswer == null || correctAnswer == null) {
      return false;
    }

    String normalizedStudent = studentAnswer.trim().toUpperCase();
    String normalizedCorrect = correctAnswer.trim().toUpperCase();

    return normalizedStudent.equals(normalizedCorrect);
  }
}