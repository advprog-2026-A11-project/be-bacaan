package id.ac.ui.cs.advprog.yomu.controller;

import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionResponse;
import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitRequest;
import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitResponse;
import id.ac.ui.cs.advprog.yomu.entity.Question;
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

    List<Question> questions = studentQuizService.getQuizQuestions(userId, readingId);
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
