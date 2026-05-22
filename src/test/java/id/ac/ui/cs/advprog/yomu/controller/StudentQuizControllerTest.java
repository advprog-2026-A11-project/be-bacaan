package id.ac.ui.cs.advprog.yomu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import id.ac.ui.cs.advprog.yomu.dto.QuizResultResponse;
import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitRequest;
import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitResponse;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.service.QuizService;
import id.ac.ui.cs.advprog.yomu.service.StudentQuizService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.*;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StudentQuizControllerTest {

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule());
  private MockMvc mockMvc;

  @Mock
  private StudentQuizService studentQuizService;

  @Mock
  private QuizService quizService;

  @InjectMocks
  private StudentQuizController controller;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void getQuizQuestionTest() throws Exception {
    Question q1 = new Question();
    q1.setId("q1");
    q1.setText("Question 1");
    q1.setQuestionType("MULTIPLE_CHOICE");
    q1.setOptions(List.of("A", "B"));

    Question q2 = new Question();
    q2.setId("q2");
    q2.setText("Question 2");
    q2.setQuestionType("TRUE_FALSE");
    q2.setOptions(List.of("True", "False"));
    String userId = "user123";
    String readingId = "reading1";

    when(studentQuizService.getQuizQuestion(userId, readingId)).thenReturn(List.of(q1, q2));

    mockMvc.perform(get("/api/student/quiz/readings/{readingId}/questions",
            readingId).header("userId", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))

        .andExpect(jsonPath("$[0].id").value("q1"))
        .andExpect(jsonPath("$[0].text").value("Question 1"))
        .andExpect(jsonPath("$[0].questionType").value("MULTIPLE_CHOICE"))

        .andExpect(jsonPath("$[1].id").value("q2"))
        .andExpect(jsonPath("$[1].text").value("Question 2"))
        .andExpect(jsonPath("$[1].questionType").value("TRUE_FALSE"));

    verify(studentQuizService, times(1)).getQuizQuestion(userId, readingId);
  }

  @Test
  void getQuizQuestionUserIdInvalidFormat() throws Exception {
    String[] invalidUserIds = {"user@123", "user 123", "user_123", "", " "};

    for (String invalidId : invalidUserIds) {
      mockMvc.perform(get("/api/student/quiz/readings/{readingId}/questions", "r1")
              .header("userId", invalidId))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("Invalid user id format"));
    }
  }

  @Test
  void submitQuizTest() throws Exception {
    Map<String, String> answers = new HashMap<>();
    answers.put("q1", "A");

    QuizSubmitRequest request = new QuizSubmitRequest();
    request.setAnswers(answers);
    request.setTimeTakenSeconds(120);

    Map<String, Boolean> results = new HashMap<>();
    results.put("q1", true);

    QuizSubmitResponse response = QuizSubmitResponse.builder()
        .score(100)
        .accuracy(100)
        .correctAnswers(1)
        .totalQuestions(1)
        .timeTaken(120)
        .questionResults(results)
        .build();

    String userId = "user123";
    String readingId = "reading1";

    when(studentQuizService.submitQuiz(
        eq(userId),
        eq(readingId),
        any(QuizSubmitRequest.class)))
        .thenReturn(response);

    mockMvc.perform(post("/api/student/quiz/readings/{readingId}/submit", readingId)
            .header("userId", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.score").value(100))
        .andExpect(jsonPath("$.accuracy").value(100))
        .andExpect(jsonPath("$.correctAnswers").value(1))
        .andExpect(jsonPath("$.totalQuestions").value(1))
        .andExpect(jsonPath("$.timeTaken").value(120))
        .andExpect(jsonPath("$.questionResults.q1").value(true));

    verify(studentQuizService, times(1))
        .submitQuiz(eq(userId), eq(readingId), any(QuizSubmitRequest.class));
  }

  @Test
  void submitQuiz_whenUserIdInvalidFormat_shouldReturnBadRequest() throws Exception {
    String[] invalidUserIds = {"invalid@user", "invalid user", "invalid_user", "", " "};
    QuizSubmitRequest request = new QuizSubmitRequest();
    request.setAnswers(Map.of("q1", "A"));
    request.setTimeTakenSeconds(100);

    for (String invalidId : invalidUserIds) {
      mockMvc.perform(post("/api/student/quiz/readings/{readingId}/submit", "r1")
              .header("userId", invalidId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("Invalid user id format"));
    }
  }

  // =============================
  // GET /readings/{readingId}/result tests
  // =============================

  @Test
  void getQuizResult_shouldReturnResultWithCorrectAnswers() throws Exception {
    String userId = "user123";
    String readingId = "reading1";

    QuizResultResponse.QuestionResultDetail detail1 = QuizResultResponse.QuestionResultDetail
        .builder()
        .questionId("q1")
        .questionText("Apa ibu kota Indonesia?")
        .questionType("MULTIPLE_CHOICE")
        .options(List.of("Jakarta", "Bandung", "Surabaya"))
        .userAnswer("Jakarta")
        .correctAnswer("Jakarta")
        .isCorrect(true)
        .build();

    QuizResultResponse.QuestionResultDetail detail2 = QuizResultResponse.QuestionResultDetail
        .builder()
        .questionId("q2")
        .questionText("Bumi berbentuk bulat?")
        .questionType("TRUE_FALSE")
        .options(List.of("True", "False"))
        .userAnswer("False")
        .correctAnswer("True")
        .isCorrect(false)
        .build();

    QuizResultResponse resultResponse = QuizResultResponse.builder()
        .readingId(readingId)
        .score(50)
        .accuracy(50)
        .totalQuestions(2)
        .correctAnswers(1)
        .completedAt(LocalDateTime.of(2026, 5, 20, 10, 0))
        .questionDetails(List.of(detail1, detail2))
        .build();

    when(quizService.getQuizResult(userId, readingId)).thenReturn(resultResponse);

    mockMvc.perform(get("/api/student/quiz/readings/{readingId}/result", readingId)
            .header("userId", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.readingId").value(readingId))
        .andExpect(jsonPath("$.score").value(50))
        .andExpect(jsonPath("$.accuracy").value(50))
        .andExpect(jsonPath("$.totalQuestions").value(2))
        .andExpect(jsonPath("$.correctAnswers").value(1))
        .andExpect(jsonPath("$.questionDetails.length()").value(2))

        .andExpect(jsonPath("$.questionDetails[0].questionId").value("q1"))
        .andExpect(jsonPath("$.questionDetails[0].userAnswer").value("Jakarta"))
        .andExpect(jsonPath("$.questionDetails[0].correctAnswer").value("Jakarta"))
        .andExpect(jsonPath("$.questionDetails[0].correct").value(true))

        .andExpect(jsonPath("$.questionDetails[1].questionId").value("q2"))
        .andExpect(jsonPath("$.questionDetails[1].userAnswer").value("False"))
        .andExpect(jsonPath("$.questionDetails[1].correctAnswer").value("True"))
        .andExpect(jsonPath("$.questionDetails[1].correct").value(false));

    verify(quizService, times(1)).getQuizResult(userId, readingId);
  }

  @Test
  void getQuizResult_whenInvalidUserId_shouldReturnBadRequest() throws Exception {
    String[] invalidUserIds = {"user@123", "user 123", "user_123", "", " "};

    for (String invalidId : invalidUserIds) {
      mockMvc.perform(get("/api/student/quiz/readings/{readingId}/result", "r1")
              .header("userId", invalidId))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("Invalid user id format"));
    }
  }

  @Test
  void getQuizResult_whenQuizNotCompleted_shouldReturnBadRequest() throws Exception {
    String userId = "user123";
    String readingId = "reading1";

    when(quizService.getQuizResult(userId, readingId))
        .thenThrow(new IllegalArgumentException(
            "Quiz result not found. User has not completed this quiz."));

    mockMvc.perform(get("/api/student/quiz/readings/{readingId}/result", readingId)
            .header("userId", userId))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Quiz result not found. User has not completed this quiz."));
  }
}