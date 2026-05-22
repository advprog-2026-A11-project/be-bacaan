package id.ac.ui.cs.advprog.yomu.controller;

import id.ac.ui.cs.advprog.yomu.dto.CompletedQuizRequest;
import id.ac.ui.cs.advprog.yomu.dto.ReadingResponse;
import id.ac.ui.cs.advprog.yomu.dto.UserStatsResponse;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.service.QuizService;
import id.ac.ui.cs.advprog.yomu.service.StudentReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/readings")
@RequiredArgsConstructor
public class StudentReadingController {

  private final QuizService quizService;
  private final StudentReadingService studentReadingService;

  /**
   * Mendapatkan detail satu bacaan. userId diambil dari JWT token secara otomatis.
   */
  @GetMapping("/{readingId}")
  public ResponseEntity<ReadingResponse> getReading(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable String readingId) {

    String userId = getAuthenticatedUserId(jwt);

    if (!isValidId(readingId)) {
      throw new IllegalArgumentException("Invalid Reading ID format");
    }

    Reading reading = studentReadingService.getReading(userId, readingId);

    ReadingResponse response = ReadingResponse.builder()
        .id(reading.getId())
        .title(reading.getTitle())
        .content(reading.getContent())
        .category(reading.getCategory())
        .difficultyLevel(reading.getDifficultyLevel())
        .quizDurationMinutes(reading.getQuizDurationMinutes())
        .build();

    return ResponseEntity.ok(response);
  }

  /**
   * Menandai bacaan sebagai selesai dan menyimpan skor quiz.
   * userId diambil dari JWT token secara otomatis.
   */
  @PostMapping("/{readingId}/complete")
  public ResponseEntity<String> completeQuiz(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable String readingId,
      @RequestBody CompletedQuizRequest request) {

    String userId = getAuthenticatedUserId(jwt);

    if (!isValidId(readingId)) {
      return ResponseEntity.badRequest().body("Invalid Reading ID format");
    }

    quizService.completeQuiz(userId, readingId,
        request.getScore(), request.getAccuracy());
    return ResponseEntity.ok("Thank you for completing the quiz!");
  }

  /**
   * Mendapatkan statistik user yang sedang login.
   * userId diambil dari JWT token, bukan dari path variable,
   * agar user tidak bisa melihat statistik orang lain.
   */
  @GetMapping("/stats")
  public ResponseEntity<UserStatsResponse> getUserStats(
      @AuthenticationPrincipal Jwt jwt) {

    String userId = getAuthenticatedUserId(jwt);
    return ResponseEntity.ok(studentReadingService.getUserStats(userId));
  }

  /**
   * Mendapatkan semua bacaan. Endpoint ini bersifat publik (tanpa login).
   */
  @GetMapping
  public ResponseEntity<List<Reading>> getAllReadings() {
    return ResponseEntity.ok(studentReadingService.getAllReadings());
  }

  private boolean isValidId(String id) {
    return id != null && id.matches("^[a-zA-Z0-9-]+$");
  }

  private String getAuthenticatedUserId(Jwt jwt) {
    if (jwt == null || !isValidId(jwt.getSubject())) {
      throw new IllegalArgumentException("Invalid User ID format");
    }
    return jwt.getSubject();
  }
}
