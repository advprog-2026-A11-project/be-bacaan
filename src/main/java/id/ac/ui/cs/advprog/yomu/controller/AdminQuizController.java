package id.ac.ui.cs.advprog.yomu.controller;

import id.ac.ui.cs.advprog.yomu.dto.AdminQuestionResponse;
import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.service.AdminQuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/admin/readings/{readingId}/questions")
@RequiredArgsConstructor
public class AdminQuizController {

  private final AdminQuizService adminQuizService;

  @PostMapping
  public ResponseEntity<AdminQuestionResponse> addQuestion(
      @PathVariable String readingId,
      @Valid @RequestBody QuizQuestionRequest request) {

    Question question = adminQuizService.addQuestion(readingId, request);
    AdminQuestionResponse response = mapToAdminResponse(question);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<List<AdminQuestionResponse>> getAllQuestions(
      @PathVariable String readingId) {

    List<Question> questions = adminQuizService.getAllQuestionsForReading(readingId);

    List<AdminQuestionResponse> responses = questions.stream()
        .map(this::mapToAdminResponse)
        .collect(Collectors.toList());

    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{questionId}")
  public ResponseEntity<AdminQuestionResponse> getQuestion(
      @PathVariable String readingId,
      @PathVariable String questionId) {

    Question question = adminQuizService.getQuestion(questionId);

    // Validate that question belongs to reading
    if (!question.getReading().getId().equals(readingId)) {
      return ResponseEntity.badRequest().build();
    }

    return ResponseEntity.ok(mapToAdminResponse(question));
  }

  @PutMapping("/{questionId}")
  public ResponseEntity<Void> updateQuestion(
      @PathVariable String readingId,
      @PathVariable String questionId,
      @Valid @RequestBody QuizQuestionRequest request) {

    Question question = adminQuizService.getQuestion(questionId);

    // Validate that question belongs to reading
    if (!question.getReading().getId().equals(readingId)) {
      return ResponseEntity.badRequest().build();
    }

    adminQuizService.updateQuestion(questionId, request);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{questionId}")
  public ResponseEntity<Void> deleteQuestion(
      @PathVariable String readingId,
      @PathVariable String questionId) {

    Question question = adminQuizService.getQuestion(questionId);

    // Validate that question belongs to reading
    if (!question.getReading().getId().equals(readingId)) {
      return ResponseEntity.badRequest().build();
    }

    adminQuizService.deleteQuestion(questionId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteAllQuestions(
      @PathVariable String readingId) {

    adminQuizService.deleteAllQuestionsForReading(readingId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/count")
  public ResponseEntity<Long> getQuestionCount(
      @PathVariable String readingId) {

    long count = adminQuizService.getQuestionCountForReading(readingId);
    return ResponseEntity.ok(count);
  }

  // ==================== PRIVATE HELPER METHODS ====================

  /**
   * Map Question entity ke AdminQuestionResponse (dengan correctAnswer)
   */
  private AdminQuestionResponse mapToAdminResponse(Question question) {
    return AdminQuestionResponse.builder()
        .id(question.getId())
        .text(question.getText())
        .questionType(question.getQuestionType())
        .options(question.getOptions())
        .correctAnswer(question.getCorrectAnswer()) // Admin boleh melihat jawaban benar
        .build();
  }
}