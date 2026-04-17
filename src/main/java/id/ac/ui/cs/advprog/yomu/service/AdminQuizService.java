package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.repository.QuizRepository;
import id.ac.ui.cs.advprog.yomu.repository.ReadingRepository;
import id.ac.ui.cs.advprog.yomu.validator.QuizValidator;
import id.ac.ui.cs.advprog.yomu.validator.QuizValidatorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminQuizService {
  private final QuizRepository quizRepository;
  private final ReadingRepository readingRepository;
  private final QuizValidatorFactory validatorFactory;

  @Transactional
  public Question addQuestion(String readingId, QuizQuestionRequest request) {
    validateReadingExists(readingId);

    QuizValidator validator = validatorFactory
        .getValidator(request.getQuestionType());
    validator.validate(request);

    Reading reading = findReadingById(readingId);
    Question question = validator.createQuestion(request, reading);

    return quizRepository.save(question);
  }

  @Transactional
  public void updateQuestion(String questionId, QuizQuestionRequest request) {
    Question question = findQuestionById(questionId);

    // Update text if provided
    if (request.getText() != null && !request.getText().trim().isEmpty()) {
      validateQuestionText(request.getText());
      question.setText(request.getText());
    }

    // Handle question type change
    if (request.getQuestionType() != null && !request.getQuestionType().isEmpty()
        && !request.getQuestionType().equals(question.getQuestionType())) {

      QuizValidator newValidator = validatorFactory
          .getValidator(request.getQuestionType());
      request.setText(question.getText()); // Preserve text
      newValidator.validate(request);

      // Update question fields
      question.setQuestionType(request.getQuestionType());
      newValidator.updateQuestion(question, request);
    } else {
      // Same type - use existing validator
      QuizValidator validator = validatorFactory
          .getValidator(question.getQuestionType());
      validator.updateQuestion(question, request);
    }

    quizRepository.save(question);
  }

  @Transactional
  public void deleteQuestion(String questionId) {
    if (!quizRepository.existsById(questionId)) {
      throw new IllegalArgumentException(
          "Question not found with id: " + questionId);
    }
    quizRepository.deleteById(questionId);
  }

  @Transactional
  public void deleteAllQuestionsForReading(String readingId) {
    validateReadingExists(readingId);
    List<Question> questions = quizRepository.findByReading_Id(readingId);
    if (!questions.isEmpty()) {
      quizRepository.deleteAll(questions);
    }
  }

  public List<Question> getAllQuestionsForReading(String readingId) {
    validateReadingExists(readingId);
    return quizRepository.findByReading_Id(readingId);
  }

  public Question getQuestion(String questionId) {
    return findQuestionById(questionId);
  }

  public long getQuestionCountForReading(String readingId) {
    validateReadingExists(readingId);
    return quizRepository.countByReading_Id(readingId);
  }

  // ==================== PRIVATE METHODS ====================

  private void validateReadingExists(String readingId) {
    if (!readingRepository.existsById(readingId)) {
      throw new IllegalArgumentException(
          "Reading not found with id: " + readingId);
    }
  }

  private Reading findReadingById(String readingId) {
    return readingRepository.findById(readingId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Reading not found with id: " + readingId));
  }

  private Question findQuestionById(String questionId) {
    return quizRepository.findById(questionId)
        .orElseThrow(() -> new IllegalArgumentException(
            "Question not found with id: " + questionId));
  }

  private void validateQuestionText(String text) {
    if (text == null || text.trim().isEmpty()) {
      throw new IllegalArgumentException("Question text cannot be empty");
    }
    if (text.length() < 5) {
      throw new IllegalArgumentException(
          "Question text must be at least 5 characters");
    }
    if (text.length() > 500) {
      throw new IllegalArgumentException(
          "Question text cannot exceed 500 characters");
    }
  }
}