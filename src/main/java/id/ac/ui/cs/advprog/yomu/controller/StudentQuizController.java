package id.ac.ui.cs.advprog.yomu.controller;

import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionResponse;
import id.ac.ui.cs.advprog.yomu.dto.QuizResultResponse;
import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitRequest;
import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitResponse;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.service.QuizService;
import id.ac.ui.cs.advprog.yomu.service.StudentQuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student/quiz")
@RequiredArgsConstructor
public class StudentQuizController {

  private final StudentQuizService studentQuizService;
  private final QuizService quizService;

  @GetMapping("/readings/{readingId}/questions")
  public ResponseEntity<List<QuizQuestionResponse>> getQuizQuestion(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable String readingId) {

    String userId = jwt.getSubject();

    List<Question> questions = studentQuizService.getQuizQuestion(userId, readingId);
    List<QuizQuestionResponse> responses = questions.stream()
        .map(this::mapToStudentResponse)
        .collect(Collectors.toList());

    return ResponseEntity.ok(responses);
  }

  @PostMapping("/readings/{readingId}/submit")
  public ResponseEntity<QuizSubmitResponse> submitQuiz(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable String readingId,
      @Valid @RequestBody QuizSubmitRequest request) {

    String userId = jwt.getSubject();

    QuizSubmitResponse response = studentQuizService.submitQuiz(userId, readingId, request);
    return ResponseEntity.ok(response);
  }

  /**
   * Mendapatkan hasil quiz yang sudah dikerjakan user, lengkap dengan jawaban
   * benar per soal.
   */
  @GetMapping("/readings/{readingId}/result")
  public ResponseEntity<QuizResultResponse> getQuizResult(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable String readingId) {

    String userId = jwt.getSubject();

    QuizResultResponse result = quizService.getQuizResult(userId, readingId);
    return ResponseEntity.ok(result);
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