package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.repository.ReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReadingService {
  private final ReadingRepository readingRepository;

  public Reading createReading(Reading reading) {
    return readingRepository.save(reading);
  }

  public List<Reading> findAll() {
    return readingRepository.findAll();
  }

  @Transactional
  public void deleteReading(String id) {
    readingRepository.deleteById(id);
  }

  @Transactional
  public void updateReading(String id, Reading newReading) {
    Reading reading = readingRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("Not found"));
    reading.setTitle(newReading.getTitle());
    reading.setContent(newReading.getContent());
    reading.setCategory(newReading.getCategory());
    reading.setDifficultyLevel(newReading.getDifficultyLevel());
    reading.setQuestions(newReading.getQuestions());
  }
}
