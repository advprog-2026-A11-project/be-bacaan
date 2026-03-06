package id.ac.ui.cs.advprog.yomu.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler exceptionHandler;

  @BeforeEach
  void setUp() {
    exceptionHandler = new GlobalExceptionHandler();
  }

  @Test
  void testHandleIllegalState() {
    IllegalStateException ex = new IllegalStateException("This quiz has been completed");

    ResponseEntity<String> response = exceptionHandler.handleIllegalState(ex);

    assertNotNull(response);
    assertEquals(400, response.getStatusCodeValue());
    assertEquals("This quiz has been completed", response.getBody());
  }

  @Test
  void testHandleIllegalStateWithDifferentMessage() {
    IllegalStateException ex = new IllegalStateException("Quiz already finished");

    ResponseEntity<String> response = exceptionHandler.handleIllegalState(ex);

    assertEquals(400, response.getStatusCodeValue());
    assertEquals("Quiz already finished", response.getBody());
  }
}