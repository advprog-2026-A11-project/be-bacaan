package id.ac.ui.cs.advprog.yomu.controller;

import id.ac.ui.cs.advprog.yomu.dto.CompletedQuizRequest;
import id.ac.ui.cs.advprog.yomu.dto.ReadingResponse;
import id.ac.ui.cs.advprog.yomu.dto.UserStatsResponse;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.service.QuizService;
import id.ac.ui.cs.advprog.yomu.service.StudentReadingService;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StudentReadingControllerTest {

  @Mock
  private QuizService quizService;

  @InjectMocks
  private StudentReadingController readingController;

  @Mock
  private StudentReadingService studentReadingService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(readingController).build();
  }

  // =============================
  // GET /{readingId}
  // =============================
  @Test
  void testGetReadingSuccess() {
    String userId = "user123";
    String readingId = "reading456";

    Reading reading = new Reading();
    reading.setId(readingId);
    reading.setTitle("Sample Reading");

    when(studentReadingService.getReading(userId, readingId)).thenReturn(reading);

    ResponseEntity<?> response = readingController.getReading(jwt(userId), readingId);

    assertEquals(200, response.getStatusCodeValue());

    ReadingResponse body = (ReadingResponse) response.getBody();
    assertEquals("reading456", body.getId());
    assertEquals("Sample Reading", body.getTitle());

    verify(studentReadingService, times(1)).getReading(userId, readingId);
  }

  @Test
  void testGetReadingServiceThrowsException() {
    String userId = "user123";
    String readingId = "reading456";

    when(studentReadingService.getReading(userId, readingId))
        .thenThrow(new IllegalStateException("Quiz already completed"));

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> readingController.getReading(jwt(userId), readingId));

    assertEquals("Quiz already completed", ex.getMessage());
    verify(studentReadingService, times(1)).getReading(userId, readingId);
  }

  // =============================
  // POST /{readingId}/complete
  // =============================
  @Test
  void testCompleteQuizSuccess() {
    String userId = "user123";
    String readingId = "reading456";

    CompletedQuizRequest request = new CompletedQuizRequest();
    request.setScore(80);
    request.setAccuracy(90);

    ResponseEntity<String> response = readingController.completeQuiz(jwt(userId), readingId, request);

    assertEquals(200, response.getStatusCodeValue());
    assertEquals("Thank you for completing the quiz!", response.getBody());
    verify(quizService, times(1)).completeQuiz(userId, readingId, 80, 90);
  }

  @Test
  void testCompleteQuizInvalidUserId() {
    String invalidUserId = "user 123!"; // ada spasi & simbol
    String readingId = "reading456";

    CompletedQuizRequest request = new CompletedQuizRequest();
    request.setScore(80);
    request.setAccuracy(90);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> readingController.completeQuiz(jwt(invalidUserId), readingId, request)
    );

    assertEquals("Invalid User ID format", exception.getMessage());
    verify(quizService, never()).completeQuiz(anyString(), anyString(), anyInt(), anyInt());
  }

  @Test
  void testCompleteQuizNullUserId() {
    String readingId = "reading456";

    CompletedQuizRequest request = new CompletedQuizRequest();
    request.setScore(80);
    request.setAccuracy(90);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> readingController.completeQuiz(null, readingId, request)
    );

    assertEquals("Invalid User ID format", exception.getMessage());
    verify(quizService, never()).completeQuiz(anyString(), anyString(), anyInt(), anyInt());
  }

  @Test
  void testCompleteQuizInvalidReadingId() {
    String userId = "user123";
    String invalidReadingId = "reading 456!";

    CompletedQuizRequest request = new CompletedQuizRequest();
    request.setScore(80);
    request.setAccuracy(90);

    ResponseEntity<String> response = readingController
        .completeQuiz(jwt(userId), invalidReadingId, request);

    assertEquals(400, response.getStatusCodeValue());
    assertEquals("Invalid Reading ID format", response.getBody());
    verify(quizService, never())
        .completeQuiz(anyString(), anyString(), anyInt(), anyInt());
  }

  @Test
  void testCompleteQuizNullReadingId() {
    String userId = "user123";

    CompletedQuizRequest request = new CompletedQuizRequest();
    request.setScore(80);
    request.setAccuracy(90);

    ResponseEntity<String> response = readingController
        .completeQuiz(jwt(userId), null, request);

    assertEquals(400, response.getStatusCodeValue());
    assertEquals("Invalid Reading ID format", response.getBody());
    verify(quizService, never())
        .completeQuiz(anyString(), anyString(), anyInt(), anyInt());
  }

  @Test
  void testCompleteQuizServiceThrowsException() {
    String userId = "user123";
    String readingId = "reading456";

    CompletedQuizRequest request = new CompletedQuizRequest();
    request.setScore(80);
    request.setAccuracy(90);

    doThrow(new IllegalStateException("This quiz has been completed"))
        .when(quizService)
        .completeQuiz(userId, readingId, 80, 90);

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> readingController.completeQuiz(jwt(userId), readingId, request));

    assertEquals("This quiz has been completed", ex.getMessage());
    verify(quizService, times(1))
        .completeQuiz(userId, readingId, request.getScore(), request.getAccuracy());
  }

  @Test
  void testGetReadingInvalidUserId() {
    String invalidUserId = "user 123!";
    String readingId = "reading456";

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> readingController.getReading(jwt(invalidUserId), readingId)
    );

    assertEquals("Invalid User ID format", exception.getMessage());
    verify(studentReadingService, never()).getReading(anyString(), anyString());
  }

  @Test
  void testGetReadingInvalidReadingId() {
    String userId = "user123";
    String invalidReadingId = "reading 456!";

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> readingController.getReading(jwt(userId), invalidReadingId)
    );

    assertEquals("Invalid Reading ID format", exception.getMessage());
    verify(studentReadingService, never()).getReading(anyString(), anyString());
  }

  @Test
  void testGetReadingWithDashInReadingId() {
    String userId = "user123";
    String readingId = "reading-456";

    Reading reading = new Reading();
    reading.setId(readingId);
    reading.setTitle("Sample Reading");

    when(studentReadingService.getReading(userId, readingId)).thenReturn(reading);

    ResponseEntity<?> response = readingController.getReading(jwt(userId), readingId);

    assertEquals(200, response.getStatusCodeValue());
    verify(studentReadingService, times(1)).getReading(userId, readingId);
  }

  @Test
  void testGetAllReadingsForStudent() throws Exception {
    when(studentReadingService.getAllReadings()).thenReturn(List.of());

    mockMvc.perform(get("/api/student/readings")
            .header("userId", "user123"))
        .andExpect(status().isOk());
  }

  @Test
  void testGetUserStatsSuccess() {
    String userId = "user-123";

    UserStatsResponse mockResponse = UserStatsResponse.builder()
        .totalCompleted(10)
        .completionFrequency(50)
        .averageAccuracy(85.0)
        .build();

    when(studentReadingService.getUserStats(userId)).thenReturn(mockResponse);


    ResponseEntity<UserStatsResponse> response =
        readingController.getUserStats(jwt(userId));

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(mockResponse, response.getBody());

    verify(studentReadingService, times(1)).getUserStats(userId);
  }

  private Jwt jwt(String userId) {
    Jwt.Builder builder = Jwt.withTokenValue("token")
        .header("alg", "none");
    if (userId != null) {
      builder.subject(userId);
    }
    return builder.build();
  }
}
