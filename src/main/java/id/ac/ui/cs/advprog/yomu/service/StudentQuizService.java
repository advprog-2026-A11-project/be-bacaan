package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitRequest;
import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitResponse;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.repository.QuizRepository;
import id.ac.ui.cs.advprog.yomu.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentQuizService {
  private final QuizRepository quizRepository;
  private final UserProgressRepository userProgressRepository;
  private final QuizService quizService;

  public List<Question> getQuizQuestion(String userId, String readingId) {
    if (userProgressRepository.existsByUserIdAndReadingId(userId, readingId)) {
      throw new IllegalStateException(
          "You've completed this quiz"
      );
    }

    return quizRepository.findByReadingId(readingId);
  }

  public QuizSubmitResponse submitQuiz(String userId, String readingId,
                                       QuizSubmitRequest request) {

    List<Question> questions = quizRepository.findByReadingId(readingId);

    Map<String, String> studentAnswers = request.getAnswers();

    int correctCount = 0;
    Map<String, Boolean> questionResults = new HashMap<>();
    for (Question question : questions) {
      String questId = question.getId();
      String studentAnswer = studentAnswers.get(questId);
      String correctAnswer = question.getCorrectAnswer();

      boolean isCorrect = isAnswerCorrect(studentAnswer, correctAnswer);
      questionResults.put(questId, isCorrect);

      if(isCorrect) {
        correctCount++;
      }
    }

    int totalQuestions = questions.size();
    int score = 0;
    double accuracy = 0.0;

    if (totalQuestions > 0) {
      score = (int) Math.round((double) correctCount / totalQuestions * 100);
      accuracy = (double) correctCount / totalQuestions;
    }

    quizService.completeQuiz(userId, readingId, score, accuracy);

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
  private boolean isAnswerCorrect(String studentAnswer, String correctAnswer) {
    if(studentAnswer == null || correctAnswer == null) {
      return false;
    }

    String normalizedStudent = studentAnswer.trim().toUpperCase();
    String normalizedCorrect = correctAnswer.trim().toUpperCase();

    return normalizedStudent.equals(normalizedCorrect);
  }
}
