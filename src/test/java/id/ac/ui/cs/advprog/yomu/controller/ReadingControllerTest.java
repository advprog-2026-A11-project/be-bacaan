package id.ac.ui.cs.advprog.yomu.controller;

import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReadingController.class)
class ReadingControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private QuizService quizService;

  private Reading reading;

  @BeforeEach
  void setUp() {
    reading = new Reading();
    reading.setId("read1");
    reading.setTitle("Test Title");
    reading.setContent("Test Content");
    reading.setCategory("Category A");
    reading.setDifficultyLevel("Easy");
  }

  @Test
  void testGetReadingSuccess() throws Exception {
    when(quizService.getReading("user1", "read1")).thenReturn(reading);

    mockMvc.perform(get("/api/student/readings/{readingId}", "read1")
            .header("userId", "user1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("read1"))
        .andExpect(jsonPath("$.title").value("Test Title"));

    verify(quizService, times(1)).getReading("user1", "read1");
  }

  @Test
  void testGetReadingAlreadyCompleted() throws Exception {
    when(quizService.getReading("user1", "read1"))
        .thenThrow(new IllegalStateException("Congratulations! You've completed this quiz!"));

    mockMvc.perform(get("/api/student/readings/{readingId}", "read1")
            .header("userId", "user1"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Congratulations! You've completed this quiz!"));

    verify(quizService, times(1)).getReading("user1", "read1");
  }

  @Test
  void testCompleteQuizSuccess() throws Exception {
    doNothing().when(quizService).completeQuiz("user1", "read1");

    mockMvc.perform(post("/api/student/readings/{readingId}/complete", "read1")
            .header("userId", "user1"))
        .andExpect(status().isOk())
        .andExpect(content().string("Thank you for completing the quiz!"));

    verify(quizService, times(1)).completeQuiz("user1", "read1");
  }

  @Test
  void testCompleteQuizAlreadyCompleted() throws Exception {
    doThrow(new IllegalStateException("This quiz has been completed"))
        .when(quizService).completeQuiz("user1", "read1");

    mockMvc.perform(post("/api/student/readings/{readingId}/complete", "read1")
            .header("userId", "user1"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("This quiz has been completed"));

    verify(quizService, times(1)).completeQuiz("user1", "read1");
  }
}