package id.ac.ui.cs.advprog.yomu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitRequest;
import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitResponse;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.service.StudentQuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class StudentQuizControllerTest {

  private MockMvc mockMvc;

  @Mock
  private StudentQuizService studentQuizService;

  @InjectMocks
  private StudentQuizController studentQuizController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private String userId;
  private String readingId;
  private Question question;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders
        .standaloneSetup(studentQuizController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();

    userId = "user-001";
    readingId = "reading-001";

    Reading reading = new Reading();
    reading.setId(readingId);

    question = new Question();
    question.setId("q-001");
    question.setText("Who invented telephone?");
    question.setQuestionType("MULTIPLE_CHOICE");
    question.setOptions(List.of("Edison", "Bell", "Tesla", "Newton"));
    question.setCorrectAnswer("B");
    question.setReading(reading);
  }

  @Test
  void testGetQuizQuestionsWhenNotCompleted() throws Exception{
    when(studentQuizService.getQuizQuestion(userId, readingId))
        .thenReturn(List.of(question));

    mockMvc.perform(get("/api/student/quiz/readings/{readingId}/questions", readingId)
        .header("userId", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value("q-001"))
        .andExpect(jsonPath("$[0].questionType").value("MULTIPLE_CHOICE"))
        .andExpect(jsonPath("$[0].correctAnswer").doesNotExist());

    verify(studentQuizService, times(1)).getQuizQuestion(userId, readingId);
  }

  @Test
  void testGetQuizQuestionsWhenAlreadyCompleted() throws Exception {
    when(studentQuizService.getQuizQuestion(userId, readingId))
        .thenThrow(new IllegalStateException("You've completed this quiz"));

    mockMvc.perform(get("/api/student/quiz/readings/{readingId}/questions", readingId)
            .header("userId", userId))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testGetQuizQuestionsWhenEmptyList() throws Exception {
    when(studentQuizService.getQuizQuestion(userId, readingId))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/student/quiz/readings/{readingId}/questions", readingId)
        .header("userId", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void testSubmitQuiz_WhenValidAnswers_Return200WithResult() throws Exception {
    QuizSubmitRequest request = new QuizSubmitRequest();
    request.setAnswers(Map.of("q-001", "B"));
    request.setTimeTakenSeconds(120);

    QuizSubmitResponse mockResponse = QuizSubmitResponse.builder()
        .score(100)
        .accuracy(1.0)
        .totalQuestions(1)
        .correctAnswers(1)
        .timeTaken(120)
        .questionResults(Map.of("q-001", true))
        .build();

    when(studentQuizService.submitQuiz(eq(userId), eq(readingId), any(QuizSubmitRequest.class)))
        .thenReturn(mockResponse);

    mockMvc.perform(post("/api/student/quiz/readings/{readingId}/submit", readingId)
            .header("userId", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.score").value(100))
        .andExpect(jsonPath("$.accuracy").value(1.0))
        .andExpect(jsonPath("$.totalQuestions").value(1))
        .andExpect(jsonPath("$.correctAnswers").value(1))
        .andExpect(jsonPath("$.questionResults.q-001").value(true));

    verify(studentQuizService, times(1))
        .submitQuiz(eq(userId), eq(readingId), any(QuizSubmitRequest.class));
  }

  @Test
  void testSubmitQuizWhenAlreadySubmitted() throws Exception {
    QuizSubmitRequest request = new QuizSubmitRequest();
    request.setAnswers(Map.of("q-001", "B"));
    request.setTimeTakenSeconds(60);

    when(studentQuizService.submitQuiz(eq(userId), eq(readingId), any()))
        .thenThrow(new IllegalStateException("You've completed this quiz"));

    mockMvc.perform(post("/api/student/quiz/readings/{readingId}/submit", readingId)
        .header("userId", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testSubmitQuizWhenPartialCorrect() throws Exception {
    QuizSubmitRequest request = new QuizSubmitRequest();
    request.setAnswers(Map.of("q-001", "A", "q-002", "False"));
    request.setTimeTakenSeconds(90);

    QuizSubmitResponse mockResponse = QuizSubmitResponse.builder()
        .score(50)
        .accuracy(0.5)
        .totalQuestions(2)
        .correctAnswers(1)
        .timeTaken(90)
        .questionResults(Map.of("q-001", false, "q-002", true))
        .build();

    when(studentQuizService.submitQuiz(eq(userId), eq(readingId), any()))
        .thenReturn(mockResponse);

    mockMvc.perform(post("/api/student/quiz/readings/{readingId}/submit", readingId)
        .header("userId", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.score").value(50))
        .andExpect(jsonPath("$.accuracy").value(0.5))
        .andExpect(jsonPath("$.correctAnswers").value(1));
  }

}
