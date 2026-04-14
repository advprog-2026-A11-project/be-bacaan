package id.ac.ui.cs.advprog.yomu.controller;

import id.ac.ui.cs.advprog.yomu.dto.CompletedQuizRequest;
import id.ac.ui.cs.advprog.yomu.dto.ReadingResponse;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.entity.UserProgress;
import id.ac.ui.cs.advprog.yomu.repository.UserProgressRepository;
import id.ac.ui.cs.advprog.yomu.service.AdminReadingService;
import id.ac.ui.cs.advprog.yomu.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/readings")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ReadingController {

  private final QuizService quizService;
  private final AdminReadingService adminService;
  private final UserProgressRepository userProgressRepository;

  @GetMapping("/{readingId}")
  public ResponseEntity<?> getReading(@RequestHeader("userId") String userId,
                                      @PathVariable String readingId) {

    if (userId == null || !userId.matches("^[a-zA-Z0-9]+$")) {
      return ResponseEntity.status(400).body("Invalid User ID format");
    }

    if (readingId == null || !readingId.matches("^[a-zA-Z0-9-]+$")) {
      return ResponseEntity.status(400).body("Invalid Reading ID format");
    }

    Reading reading = adminService.getById(readingId);

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
  public ResponseEntity<String> completeQuiz(@RequestHeader("userId") String userId,
                                             @PathVariable String readingId,
                                             @RequestBody CompletedQuizRequest request) {
    if (userId == null || !userId.matches("^[a-zA-Z0-9]+$")) {
      return ResponseEntity.status(400).body("Invalid User ID format");
    }

    if (readingId == null || !readingId.matches("^[a-zA-Z0-9-]+$")) {
      return ResponseEntity.status(400).body("Invalid Reading ID format");
    }

    quizService.completeQuiz(userId, readingId,
        request.getScore(), request.getAccuracy());
    return ResponseEntity.ok("Thank you for completing the quiz!");
  }

  @GetMapping("/stats/{userId}")
  public ResponseEntity<?> getUserStats(@PathVariable String userId) {
    if (userId == null || !userId.matches("^[a-zA-Z0-9]+$")) {
      return ResponseEntity.status(400).body("Invalid User ID format");
    }

    List<UserProgress> progresses = userProgressRepository.findByUserId(userId);

    long totalCompleted = progresses.size();
    double avgAccuracy = progresses.stream()
        .mapToDouble(UserProgress::getAccuracy)
        .average()
        .orElse(0.0);

    Map<String, Object> stats = new HashMap<>();
    stats.put("userId", userId);
    stats.put("totalCompleted", totalCompleted);
    stats.put("completionFrequency", totalCompleted);
    stats.put("averageAccuracy", avgAccuracy);

    return ResponseEntity.ok(stats);
  }

  // get all readings
  @GetMapping
  public ResponseEntity<List<Reading>> getAllReadings(@RequestHeader("userId") String userId) {
    return ResponseEntity.ok(adminService.findAll());
  }
}
