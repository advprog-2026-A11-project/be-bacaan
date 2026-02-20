package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.entity.TextContext;
import id.ac.ui.cs.advprog.yomu.repository.TextContextRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TextContextServiceImpl implements TextContextService {

  TextContextRepository repository;

  public  TextContextServiceImpl(TextContextRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<TextContext> findAll() {
    return repository.findAll();
  }

  @Override
  public Optional<TextContext> findById(String id) {
    return repository.findById(id);
  }
}
