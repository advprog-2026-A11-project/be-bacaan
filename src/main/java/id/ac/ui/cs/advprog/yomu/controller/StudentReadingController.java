package id.ac.ui.cs.advprog.yomu.controller;

import id.ac.ui.cs.advprog.yomu.dto.CompletedQuizRequest;
import id.ac.ui.cs.advprog.yomu.dto.ReadingResponse;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.security.YomulPrincipal;
import id.ac.ui.cs.advprog.yomu.service.QuizService;
import id.ac.ui.cs.advprog.yomu.service.StudentReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/student/readings")
@RequiredArgsConstructor
public class StudentReadingController {

  private final QuizService quizService;
  private final StudentReadingService studentReadingService;

  @GetMapping("/{readingId}")
  public ResponseEntity<?> getReading(@RequestHeader(value = "userId") String userId,
                                      @PathVariable String readingId) {

    if (!isValidId(userId)) {
      return ResponseEntity.badRequest().body("Invalid User ID format");
    }

    if (!isValidId(readingId)) {
      return ResponseEntity.badRequest().body("Invalid Reading ID format");
    }

    Reading reading = studentReadingService.getReading(userId, readingId);

    ReadingResponse response = ReadingResponse.builder()
        .id(reading.getId())
        .title(reading.getTitle())
        .content(reading.getContent())
        .category(reading.getCategory())
        .difficultyLevel(reading.getDifficultyLevel())
        .build();

    return ResponseEntity.ok(response);
  }

  @PostMapping("/{readingId}/complete")
  public ResponseEntity<String> completeQuiz(@RequestHeader String userId,
                                             @PathVariable String readingId,
                                             @RequestBody CompletedQuizRequest request) {

    if (!isValidId(userId)) {
      return ResponseEntity.badRequest().body("Invalid User ID format");
    }

    if (!isValidId(readingId)) {
      return ResponseEntity.badRequest().body("Invalid Reading ID format");
    }

    quizService.completeQuiz(userId, readingId,
        request.getScore(), request.getAccuracy());
    return ResponseEntity.ok("Thank you for completing the quiz!");
  }

  @GetMapping("/stats/{userId}")
  public ResponseEntity<?> getUserStats(@PathVariable String userId) {
    return ResponseEntity.ok(studentReadingService.getUserStats(userId));
  }

  // get all readings
  @GetMapping
  public ResponseEntity<List<Reading>> getAllReadings() {
    return ResponseEntity.ok(studentReadingService.getAllReadings());
  }

  private boolean isValidId(String id) {
    return id != null && id.matches("^[a-zA-Z0-9-]+$");
  }
}
