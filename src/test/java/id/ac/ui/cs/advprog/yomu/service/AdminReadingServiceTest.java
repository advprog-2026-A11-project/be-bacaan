package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.dto.ReadingRequest;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.repository.ReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminReadingServiceTest {

  @Mock
  private ReadingRepository readingRepository;

  @InjectMocks
  private AdminReadingService adminService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreateReading() {
    final ReadingRequest dto = new ReadingRequest();
    dto.setTitle("Test Title");
    dto.setContent("Test Content");
    dto.setCategory("Science");
    dto.setDifficultyLevel("Medium");

    final Reading savedReading = new Reading();
    savedReading.setTitle(dto.getTitle());
    savedReading.setContent(dto.getContent());
    savedReading.setCategory(dto.getCategory());
    savedReading.setDifficultyLevel(dto.getDifficultyLevel());

    when(readingRepository.save(any(Reading.class))).thenReturn(savedReading);

    final Reading result = adminService.createReading(dto);

    ArgumentCaptor<Reading> captor = ArgumentCaptor.forClass(Reading.class);
    verify(readingRepository).save(captor.capture());
    final Reading captured = captor.getValue();

    assertEquals(dto.getTitle(), captured.getTitle());
    assertEquals(dto.getContent(), captured.getContent());
    assertEquals(dto.getCategory(), captured.getCategory());
    assertEquals(dto.getDifficultyLevel(), captured.getDifficultyLevel());
    assertEquals(savedReading, result);
  }

  @Test
  void testFindAll() {
    final Reading r1 = new Reading();
    r1.setTitle("R1");
    final Reading r2 = new Reading();
    r2.setTitle("R2");

    when(readingRepository.findAll()).thenReturn(Arrays.asList(r1, r2));

    final List<Reading> result = adminService.findAll();

    assertEquals(2, result.size());
    assertTrue(result.contains(r1));
    assertTrue(result.contains(r2));
    verify(readingRepository, times(1)).findAll();
  }

  @Test
  void testDeleteReading() {
    final String id = "123";
    doNothing().when(readingRepository).deleteById(id);

    adminService.deleteReading(id);
    verify(readingRepository, times(1)).deleteById(id);
  }

  @Test
  void testUpdateReading() {
    final String id = "123";
    final Reading existing = new Reading();
    existing.setTitle("Old Title");
    existing.setContent("Old Content");
    existing.setCategory("Old Category");
    existing.setDifficultyLevel("Easy");

    final ReadingRequest dto = new ReadingRequest();
    dto.setTitle("New Title");
    dto.setContent("New Content");
    dto.setCategory("Math");
    dto.setDifficultyLevel("Hard");

    when(readingRepository.findById(id)).thenReturn(Optional.of(existing));

    adminService.updateReading(id, dto);

    assertEquals("New Title", existing.getTitle());
    assertEquals("New Content", existing.getContent());
    assertEquals("Math", existing.getCategory());
    assertEquals("Hard", existing.getDifficultyLevel());
  }

  @Test
  void testUpdateReadingNotFound() {
    final String id = "not-exist";
    final ReadingRequest dto = new ReadingRequest();

    when(readingRepository.findById(id)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> adminService.updateReading(id, dto));

    assertEquals("Reading not found", exception.getMessage());
  }
}