package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.entity.TextContext;
import id.ac.ui.cs.advprog.yomu.repository.TextContextRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

public interface TextContextService {

  List<TextContext> findAll();
  Optional<TextContext> findById(String id);
}
