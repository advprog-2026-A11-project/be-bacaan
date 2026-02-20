package id.ac.ui.cs.advprog.yomu.controller;

import id.ac.ui.cs.advprog.yomu.entity.TextContext;
import id.ac.ui.cs.advprog.yomu.service.TextContextService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/texts")
@CrossOrigin(origins = "http://localhost:300")
public class TextContextController {

  TextContextService service;

  public TextContextController(TextContextService service) {
    this.service = service;
  }

  @GetMapping
  public List<TextContext> getAllTexts() {
    return service.findAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<TextContext> getTextById(@PathVariable String id) {
    return service.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
