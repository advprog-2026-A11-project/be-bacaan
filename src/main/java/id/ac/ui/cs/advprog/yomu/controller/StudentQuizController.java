package id.ac.ui.cs.advprog.yomu.controller;

import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionResponse;
import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitRequest;
import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitResponse;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.service.StudentQuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/student/quiz")
@RequiredArgsConstructor
public class StudentQuizController {
  private final StudentQuizService studentQuizService;

  @GetMapping("/readings/{readingId}/questions")
  public ResponseEntity<List<QuizQuestionResponse>> getQuizQuestion(
      @RequestHeader("userId") String userId,
      @PathVariable String readingId) {

    // userId validation
    if (userId == null || !userId.matches("^[a-zA-Z0-9-]+$")) {
      throw new IllegalArgumentException("Invalid user id format");
    }

    List<Question> questions = studentQuizService.getQuizQuestion(userId, readingId);
    List<QuizQuestionResponse> responses = questions.stream()
        .map(this::mapToStudentResponse)
        .collect(Collectors.toList());

    return ResponseEntity.ok(responses);
  }

  @PostMapping("/readings/{readingId}/submit")
  public ResponseEntity<QuizSubmitResponse> submitQuiz(
      @RequestHeader("userId") String userId,
      @PathVariable String readingId,
      @Valid @RequestBody QuizSubmitRequest request) {

    // userId validation
    if (userId == null || !userId.matches("^[a-zA-Z0-9-]+$")) {
      throw new IllegalArgumentException("Invalid user id format");
    }

    QuizSubmitResponse response = studentQuizService.submitQuiz(userId, readingId, request);
    return ResponseEntity.ok(response);
  }

  // PRIVATE HELPER METHOD
  private QuizQuestionResponse mapToStudentResponse(Question question) {
    return QuizQuestionResponse.builder()
        .id(question.getId())
        .text(question.getText())
        .questionType(question.getQuestionType())
        .options(question.getOptions())
        .build();
  }
}
