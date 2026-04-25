package id.ac.ui.cs.advprog.yomu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.dto.AdminQuestionResponse;
import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.service.AdminQuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminQuizControllerTest {
  private MockMvc mockMvc;

  @Mock
  private AdminQuizService adminQuizService;

  @InjectMocks
  private AdminQuizController adminQuizController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private String readingId;
  private String questionId;
  private Reading reading;
  private Question question;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    mockMvc = MockMvcBuilders
        .standaloneSetup(adminQuizController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();

    readingId = "reading-001";
    questionId = "question-001";

    reading = new Reading();
    reading.setId(readingId);
    reading.setTitle("Planets in the solar system");

    question = new Question();
    question.setId(questionId);
    question.setText("What is the biggest planet in the solar system?");
    question.setQuestionType("MULTIPLE_CHOICE");
    question.setOptions(List.of("Earth", "Jupiter", "Saturn", "Mars"));
    question.setCorrectAnswer("B");
    question.setReading(reading);
  }

  @Test
  void testAddQuestion() throws Exception {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setText("What is the biggest planet in the solar system?");
    request.setQuestionType("MULTIPLE_CHOICE");
    request.setOptions(List.of("Earth", "Jupiter", "Saturn", "Mars"));
    request.setCorrectAnswer("B");

    when(adminQuizService.addQuestion(eq(readingId), any(QuizQuestionRequest.class)))
        .thenReturn(question);

    mockMvc.perform(post("/api/admin/readings/{readingId}/questions", readingId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(questionId))
        .andExpect(jsonPath("$.text").value("What is the biggest planet in the solar system?"))
        .andExpect(jsonPath("$.questionType").value("MULTIPLE_CHOICE"))
        .andExpect(jsonPath("$.correctAnswer").value("B"));

    verify(adminQuizService, times(1))
        .addQuestion(eq(readingId), any(QuizQuestionRequest.class));
  }

  @Test
  void testAddQuestionWhenReadingNotFound() throws Exception {
    QuizQuestionRequest request = new QuizQuestionRequest();
    request.setText("Test Questions");
    request.setQuestionType("MULTIPLE_CHOICE");
    request.setOptions(List.of("A", "B", "C"));
    request.setCorrectAnswer("A");

    when(adminQuizService.addQuestion(eq("reading-not-found"), any()))
        .thenThrow(new IllegalArgumentException("Reading not found"));

    mockMvc.perform(post("/api/admin/readings/{readingId}/questions", "reading-not-found")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testGetAllQuestionsWhenQuestionsExist() throws Exception {
    Question question2 = new Question();
    question2.setId("question-002");
    question2.setText("How many 2 + 2?");
    question2.setQuestionType("MULTIPLE_CHOICE");
    question2.setOptions(List.of("3", "4", "5"));
    question2.setCorrectAnswer("B");
    question2.setReading(reading);

    when(adminQuizService.getAllQuestionsForReading(readingId))
        .thenReturn(List.of(question, question2));

    mockMvc.perform(get("/api/admin/readings/{readingId}/questions", readingId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(questionId))
        .andExpect(jsonPath("$[1].id").value("question-002"));

    verify(adminQuizService, times(1)).getAllQuestionsForReading(readingId);
  }

  @Test
  void testGetAllQuestionsWhenNoQuestions() throws Exception {
    when(adminQuizService.getAllQuestionsForReading(readingId))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/admin/readings/{readingId}/questions", readingId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void testGetQuestionWhenQuestionBelongsToReading() throws Exception {
    when(adminQuizService.getQuestion(questionId)).thenReturn(question);

    mockMvc.perform(get("/api/admin/readings/{readingId}/questions/{questionId}",
        readingId, questionId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(questionId))
        .andExpect(jsonPath("$.text").value("What is the biggest planet in the solar system?"));
  }

  @Test
  void testGetQuestionWhenQuestionBelongsToOtherReading() throws Exception {
    Reading otherReading = new Reading();
    otherReading.setId("other-reading");
    question.setReading(otherReading);

    when(adminQuizService.getQuestion(questionId)).thenReturn(question);

    mockMvc.perform(get("/api/admin/readings/{readingId}/questions/{questionId}",
        readingId, questionId))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testUpdateQuestionValid() throws Exception {
    QuizQuestionRequest updateRequest = new QuizQuestionRequest();
    updateRequest.setText("Is this the updated question?");
    updateRequest.setQuestionType("MULTIPLE_CHOICE");
    updateRequest.setOptions(List.of("A", "B"));
    updateRequest.setCorrectAnswer("A");

    when(adminQuizService.getQuestion(questionId)).thenReturn(question);
    doNothing().when(adminQuizService).updateQuestion(eq(questionId), any());

    mockMvc.perform(put("/api/admin/readings/{readingId}/questions/{questionId}",
        readingId, questionId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk());

    verify(adminQuizService, times(1)).updateQuestion(eq(questionId), any());
  }

  @Test
  void testDeleteQuestionWhenQuestionBelongsToReading() throws Exception {
    when(adminQuizService.getQuestion(questionId)).thenReturn(question);
    doNothing().when(adminQuizService).deleteQuestion(questionId);

    mockMvc.perform(delete("/api/admin/readings/{readingId}/questions/{questionId}",
        readingId, questionId))
        .andExpect(status().isNoContent());

    verify(adminQuizService, times(1)).deleteQuestion(questionId);
  }

  @Test
  void testDeleteQuestionWhenQuestionBelongsToOtherReading() throws Exception {
    Reading otherReading = new Reading();
    otherReading.setId("other-reading");
    question.setReading(otherReading);

    when(adminQuizService.getQuestion(questionId)).thenReturn(question);

    mockMvc.perform(delete("/api/admin/readings/{readingId}/questions/{questionId}",
        readingId, questionId))
        .andExpect(status().isBadRequest());

    verify(adminQuizService, never()).deleteQuestion(anyString());
  }

  @Test
  void testDeleteAllQuestions() throws Exception {
    doNothing().when(adminQuizService).deleteAllQuestionsForReading(readingId);

    mockMvc.perform(delete("/api/admin/readings/{readingId}/questions", readingId))
        .andExpect(status().isNoContent());

    verify(adminQuizService, times(1)).deleteAllQuestionsForReading(readingId);
  }

  @Test
  void testGetQuestionCount() throws Exception {
    when(adminQuizService.getQuestionCountForReading(readingId)).thenReturn(5L);

    mockMvc.perform(get("/api/admin/readings/{readingId}/questions/count", readingId))
        .andExpect(status().isOk())
        .andExpect(content().string("5"));

    verify(adminQuizService, times(1)).getQuestionCountForReading(readingId);

  }

}
