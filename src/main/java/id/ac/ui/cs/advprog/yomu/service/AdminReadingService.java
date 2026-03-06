package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.repository.ReadingRepository;
import id.ac.ui.cs.advprog.yomu.dto.ReadingRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReadingService {
  private final ReadingRepository readingRepository;

  public Reading createReading(ReadingRequestDTO dto) {
    Reading reading = new Reading();
    reading.setTitle(dto.getTitle());
    reading.setContent(dto.getContent());
    reading.setCategory(dto.getCategory());
    reading.setDifficultyLevel(dto.getDifficultyLevel());
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
  public void updateReading(String id, ReadingRequestDTO dto) {
    Reading reading = readingRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Reading not found"));

    reading.setTitle(dto.getTitle());
    reading.setContent(dto.getContent());
    reading.setCategory(dto.getCategory());
    reading.setDifficultyLevel(dto.getDifficultyLevel());
  }
}
