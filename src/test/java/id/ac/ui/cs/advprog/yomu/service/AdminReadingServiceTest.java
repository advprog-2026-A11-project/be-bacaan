package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.repository.ReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminReadingServiceTest {

  private ReadingRepository readingRepository;
  private AdminReadingService adminReadingService;

  @BeforeEach
  void setUp() {
    readingRepository = mock(ReadingRepository.class);
    adminReadingService = new AdminReadingService(readingRepository);
  }

  @Test
  void testCreateReading() {
    Reading reading = new Reading();
    reading.setId("r1");
    reading.setTitle("Title 1");

    when(readingRepository.save(reading)).thenReturn(reading);

    Reading result = adminReadingService.createReading(reading);

    assertEquals(reading, result);
    verify(readingRepository, times(1)).save(reading);
  }

  @Test
  void testFindAll() {
    Reading r1 = new Reading();
    r1.setId("r1");
    Reading r2 = new Reading();
    r2.setId("r2");

    when(readingRepository.findAll()).thenReturn(Arrays.asList(r1, r2));

    List<Reading> result = adminReadingService.findAll();

    assertEquals(2, result.size());
    assertTrue(result.contains(r1));
    assertTrue(result.contains(r2));
    verify(readingRepository, times(1)).findAll();
  }

  @Test
  void testDeleteReading() {
    String id = "r1";

    doNothing().when(readingRepository).deleteById(id);

    adminReadingService.deleteReading(id);

    verify(readingRepository, times(1)).deleteById(id);
  }

  @Test
  void testUpdateReadingSuccess() {
    String id = "r1";
    Reading existing = new Reading();
    existing.setId(id);
    existing.setTitle("Old Title");
    existing.setContent("Old Content");
    existing.setCategory("Old Cat");
    existing.setDifficultyLevel("Easy");

    Reading updated = new Reading();
    updated.setTitle("New Title");
    updated.setContent("New Content");
    updated.setCategory("New Cat");
    updated.setDifficultyLevel("Hard");

    when(readingRepository.findById(id)).thenReturn(Optional.of(existing));

    adminReadingService.updateReading(id, updated);

    // verifikasi field di-update
    assertEquals("New Title", existing.getTitle());
    assertEquals("New Content", existing.getContent());
    assertEquals("New Cat", existing.getCategory());
    assertEquals("Hard", existing.getDifficultyLevel());
    verify(readingRepository, times(1)).findById(id);
  }

  @Test
  void testUpdateReadingNotFound() {
    String id = "r1";
    Reading updated = new Reading();

    when(readingRepository.findById(id)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class, () ->
      adminReadingService.updateReading(id, updated)
    );

    assertEquals("Not found", exception.getMessage());
    verify(readingRepository, times(1)).findById(id);
  }
}