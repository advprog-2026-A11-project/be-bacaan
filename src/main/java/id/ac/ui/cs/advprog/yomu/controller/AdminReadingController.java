package id.ac.ui.cs.advprog.yomu.controller;

import id.ac.ui.cs.advprog.yomu.dto.ReadingRequest;
import id.ac.ui.cs.advprog.yomu.dto.ReadingResponse;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.service.AdminReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/admin/readings")
@RequiredArgsConstructor
public class AdminReadingController {
  private final AdminReadingService adminService;

  @PostMapping("/create")
  // @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<Reading> create(@RequestBody ReadingRequest requestDto) {
    return ResponseEntity.ok(adminService.createReading(requestDto));
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
  public ResponseEntity<?> getById(@PathVariable String id) {
    Reading reading = adminService.getById(id);

    if (reading == null) {
      return ResponseEntity.notFound().build();
    }

    ReadingResponse response = ReadingResponse.builder()
        .id(reading.getId())
        .title(reading.getTitle())
        .content(reading.getContent())
        .category(reading.getCategory())
        .difficultyLevel(reading.getDifficultyLevel())
        .build();

    return ResponseEntity.ok(response);
  }

}
