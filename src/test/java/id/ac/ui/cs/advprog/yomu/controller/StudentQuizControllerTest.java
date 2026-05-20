package id.ac.ui.cs.advprog.yomu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitRequest;
import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitResponse;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.service.StudentQuizService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.*;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StudentQuizControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudentQuizService studentQuizService;

    @InjectMocks
    private StudentQuizController controller;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getQuizQuestionTest() throws Exception {
        String userId = "user123";
        String readingId = "reading1";

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

        when(studentQuizService.getQuizQuestion(userId, readingId)).thenReturn(List.of(q1, q2));

        mockMvc.perform(get("/api/student/quiz/readings/{readingId}/questions", readingId).header("userId", userId))
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
        String[] invalidUserIds = { "user@123", "user 123", "user_123", "", " " };

        for (String invalidId : invalidUserIds) {
            mockMvc.perform(get("/api/student/quiz/readings/{readingId}/questions", "r1")
                    .header("userId", invalidId))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Invalid user id format"));
        }
    }

    @Test
    void submitQuizTest() throws Exception {
        String userId = "user123";
        String readingId = "reading1";

        Map<String, String> answers = new HashMap<>();
        answers.put("q1", "A");

        QuizSubmitRequest request = new QuizSubmitRequest();
        request.setAnswers(answers);
        request.setTimeTakenSeconds(120);

        Map<String, Boolean> results = new HashMap<>();
        results.put("q1", true);

        QuizSubmitResponse response = QuizSubmitResponse.builder()
                .score(100)
                .accuracy(1.0)
                .correctAnswers(1)
                .totalQuestions(1)
                .timeTaken(120)
                .questionResults(results)
                .build();

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
                .andExpect(jsonPath("$.accuracy").value(1.0))
                .andExpect(jsonPath("$.correctAnswers").value(1))
                .andExpect(jsonPath("$.totalQuestions").value(1))
                .andExpect(jsonPath("$.timeTaken").value(120))
                .andExpect(jsonPath("$.questionResults.q1").value(true));

        verify(studentQuizService, times(1))
                .submitQuiz(eq(userId), eq(readingId), any(QuizSubmitRequest.class));
    }

    @Test
    void submitQuiz_whenUserIdInvalidFormat_shouldReturnBadRequest() throws Exception {
        String[] invalidUserIds = { "invalid@user", "invalid user", "invalid_user", "", " " };
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
}