package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.entity.TextContext;
import id.ac.ui.cs.advprog.yomu.repository.TextContextRepository;

import java.util.List;
import java.util.Optional;

public class TextContextService {
  TextContextRepository repository;

  public TextContextService(TextContextRepository repository) {
    this.repository = repository;
  }

  public List<TextContext> findAll() {
    return repository.findAll();
  }

  public Optional<TextContext> findById(String id) {
    return repository.findById(id);
  }
}
