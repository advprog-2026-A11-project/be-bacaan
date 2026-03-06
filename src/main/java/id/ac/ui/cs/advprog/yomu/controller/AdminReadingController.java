package id.ac.ui.cs.advprog.yomu.controller;

import id.ac.ui.cs.advprog.yomu.dto.ReadingRequest;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.service.AdminReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/readings")
@CrossOrigin(origins = "http://localhost:300")
@RequiredArgsConstructor
public class AdminReadingController {
  private final AdminReadingService adminService;

  @PostMapping("/create")
  public ResponseEntity<Reading> create(@RequestBody ReadingRequest requestDto) {
    return ResponseEntity.ok(adminService.createReading(requestDto));
  }

  @GetMapping("/reading-list")
  public ResponseEntity<List<Reading>> getAll() {
    return ResponseEntity.ok(adminService.findAll());
  }

  @PutMapping("/{id}")
  public ResponseEntity<Void> update(@PathVariable String id,
                                     @RequestBody ReadingRequest requestDto) {
    adminService.updateReading(id, requestDto);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    adminService.deleteReading(id);
    return ResponseEntity.ok().build();
  }

}
