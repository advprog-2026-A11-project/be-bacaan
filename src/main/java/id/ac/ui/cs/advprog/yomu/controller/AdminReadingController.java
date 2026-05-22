package id.ac.ui.cs.advprog.yomu.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import id.ac.ui.cs.advprog.yomu.dto.ReadingRequest;
import id.ac.ui.cs.advprog.yomu.dto.ReadingResponse;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.service.AdminReadingService;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/admin/readings")
@RequiredArgsConstructor
public class AdminReadingController {
  private final AdminReadingService adminService;

  @PostMapping("/create")
  // @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ReadingResponse> create(@RequestBody ReadingRequest requestDto) {
    Reading reading = adminService.createReading(requestDto);

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

  @GetMapping("/reading-list")
  // @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<Reading>> getAll() {
    return ResponseEntity.ok(adminService.findAll());
  }

  @PutMapping("/{id}")
  // @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> update(@PathVariable String id,
                                     @RequestBody ReadingRequest requestDto) {
    adminService.updateReading(id, requestDto);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  // @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    adminService.deleteReading(id);
    return ResponseEntity.ok().build();
  }

  // catch previous data for update reading
  @GetMapping("/{id}")
  // @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ReadingResponse> getById(@PathVariable String id) {
    Reading reading = adminService.getById(id);

    if (reading == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reading not found");
    }

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

}
