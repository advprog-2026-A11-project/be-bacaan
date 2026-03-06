package id.ac.ui.cs.advprog.yomu.controller;

import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/readings")
@CrossOrigin(origins = "http://localhost:300")
@RequiredArgsConstructor
public class ReadingController {

  private final QuizService quizService;

  @GetMapping("/{readingId}")
  public ResponseEntity<Reading> getReading(@RequestHeader("userId") String userId,
                                            @PathVariable String readingId) {
    return ResponseEntity.ok(quizService.getReading(userId, readingId));
  }

  @PostMapping("/{readingId}/complete")
  public ResponseEntity<String> completeQuiz(@RequestHeader("userId") String userId,
                                             @PathVariable String readingId) {
    if (userId == null || !userId.matches("^[a-zA-Z0-9]+$")) {
      return ResponseEntity.status(400).body("Invalid User ID format");
    }

    if (readingId == null || !readingId.matches("^[a-zA-Z0-9-]+$")) {
      return ResponseEntity.status(400).body("Invalid Reading ID format");
    }

    quizService.completeQuiz(userId, readingId);
    return ResponseEntity.ok("Thank you for completing the quiz!");
  }
}
