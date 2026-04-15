package id.ac.ui.cs.advprog.yomu.service;


import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.entity.UserProgress;
import id.ac.ui.cs.advprog.yomu.repository.ReadingRepository;
import id.ac.ui.cs.advprog.yomu.repository.UserProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentReadingServiceTest {

  @Mock
  private ReadingRepository readingRepository;

  @Mock
  private UserProgressRepository userProgressRepository;

  @InjectMocks
  private StudentReadingService studentReadingService;

  private Reading reading;

  @BeforeEach
  void setUp() {
    reading = new Reading();
    reading.setId("reading-123");
    reading.setTitle("Clean code");
    reading.setContent("Content is here...");
    reading.setCategory("Programming");
    reading.setDifficultyLevel("INTERMEDIATE");
  }

  @Test
  void testGetReadingWhenReadingExists() {
    when(readingRepository.findById("reading-123")).thenReturn(Optional.of(reading));

    Reading result = studentReadingService.getReading("user-1", "reading-123");

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo("reading-123");
    assertThat(result.getTitle()).isEqualTo("Clean code");
  }

  @Test
  void testGetReadingWhenReadingNotFound() {
    when(readingRepository.findById("not-exist")).thenReturn(Optional.empty());

    assertThatThrownBy(() ->
        studentReadingService.getReading("user-1", "not-exist"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Reading not found");
  }

  @Test
  void testGetAllReadingWhenExist() {
    Reading reading2 = new Reading();
    reading2.setId("reading-456");
    reading2.setTitle("TDD principle");

    when(readingRepository.findAll()).thenReturn(List.of(reading, reading2));

    List<Reading> result = studentReadingService.getAllReadings();

    assertThat(result).hasSize(2);
    assertThat(result).extracting(Reading::getId)
        .containsExactlyInAnyOrder("reading-123", "reading-456");
  }

  @Test
  void testGetAllReadingsEmptyList() {
    when(readingRepository.findAll()).thenReturn(List.of());

    List<Reading> result = studentReadingService.getAllReadings();

    assertThat(result).isEmpty();
  }

  @Test
  void testGetUserStatsWithCompleteReadingsReturnCorrectStats() {
    UserProgress student1 = new UserProgress();
    student1.setUserId("student-1");
    student1.setAccuracy(80.0);

    UserProgress student2 = new UserProgress();
    student2.setUserId("student-2");
    student2.setAccuracy(70.0);

    when(userProgressRepository.findByUserId("student-1")).thenReturn(List.of(student1, student2));

    Map<String, Object> stats = studentReadingService.getUserStats("student-1");

    assertThat(stats.get("userId")).isEqualTo("student-1");
    assertThat(stats.get("totalCompleted")).isEqualTo(2L);
    assertThat(stats.get("completionFrequency")).isEqualTo(2L);
    assertThat((Double) stats.get("averageAccuracy")).isEqualTo(75.0);
  }

  @Test
  void testGetUserStatsWithNoCompeletedReadingsReturnsZero() {
    when(userProgressRepository.findByUserId("new-user")).thenReturn(List.of());

    Map<String, Object> stats = studentReadingService.getUserStats("new-user");

    assertThat(stats.get("totalCompleted")).isEqualTo(0L);
    assertThat(stats.get("averageAccuracy")).isEqualTo(0.0);
  }
}
