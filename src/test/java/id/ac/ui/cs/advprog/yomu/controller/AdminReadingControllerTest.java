package id.ac.ui.cs.advprog.yomu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.dto.ReadingRequest;
import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.service.AdminReadingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminReadingControllerTest {

  private MockMvc mockMvc;

  @Mock
  private AdminReadingService adminService;

  @InjectMocks
  private AdminReadingController controller;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
      .setControllerAdvice(new GlobalExceptionHandler())
      .build();
  }


  @Test
  void createRedingTest() throws Exception {
    ReadingRequest request = new ReadingRequest();
    request.setTitle("Reading Title");
    request.setContent("Reading Content");
    request.setCategory("Technology");
    request.setDifficultyLevel("Easy");

    Reading reading = new Reading();
    reading.setId("1");
    reading.setTitle("Reading Title");
    reading.setContent("Reading Content");
    reading.setCategory("Technology");
    reading.setDifficultyLevel("Easy");

    when(adminService.createReading(any(ReadingRequest.class))).thenReturn(reading);

    mockMvc.perform(post("/api/admin/readings/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value("1"))
      .andExpect(jsonPath("$.title").value("Reading Title"))
      .andExpect(jsonPath("$.content").value("Reading Content"))
      .andExpect(jsonPath("$.category").value("Technology"))
      .andExpect(jsonPath("$.difficultyLevel").value("Easy"));

    verify(adminService, times(1)).createReading(any(ReadingRequest.class));
  }
  
  @Test
  void getAllReadings() throws Exception {
    Reading reading1 = new Reading();
    reading1.setId("1");
    reading1.setTitle("Title 1");

    Reading reading2 = new Reading();
    reading2.setId("2");
    reading2.setTitle("Title 2");

    when(adminService.findAll()).thenReturn(List.of(reading1, reading2));

    mockMvc.perform(get("/api/admin/readings/reading-list"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(2))
      .andExpect(jsonPath("$[0].id").value("1"))
      .andExpect(jsonPath("$[0].title").value("Title 1"))
      .andExpect(jsonPath("$[1].id").value("2"))
      .andExpect(jsonPath("$[1].title").value("Title 2"));

    verify(adminService, times(1)).findAll();
  }

  @Test
  void updateReadingTest() throws Exception {
    String id = "123";

    ReadingRequest request = new ReadingRequest();
    request.setTitle("Updated Title");
    request.setContent("Updated Content");

    doNothing().when(adminService).updateReading(eq(id), any(ReadingRequest.class));

    mockMvc.perform(put("/api/admin/readings/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk());

    verify(adminService, times(1)).updateReading(eq(id), any(ReadingRequest.class));
  }

  @Test
  void deleteReadingTest() throws Exception {
    String id = "123";
    doNothing().when(adminService).deleteReading(id);

    mockMvc.perform(delete("/api/admin/readings/{id}", id)).andExpect(status().isOk());
    verify(adminService, times(1)).deleteReading(id);
  }

  @Test
  void getByIdReturnReading() throws Exception {
    Reading reading = new Reading();
    reading.setId("1");
    reading.setTitle("Sample Title");
    reading.setContent("Sample Content");
    reading.setCategory("Science");
    reading.setDifficultyLevel("Medium");

    when(adminService.getById("1")).thenReturn(reading);

    mockMvc.perform(get("/api/admin/readings/{id}", "1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value("1"))
      .andExpect(jsonPath("$.title").value("Sample Title"))
      .andExpect(jsonPath("$.content").value("Sample Content"))
      .andExpect(jsonPath("$.category").value("Science"))
      .andExpect(jsonPath("$.difficultyLevel").value("Medium"));

    verify(adminService, times(1)).getById("1");
  }

  @Test
  void getByIdReadingNotFound() throws Exception {
    when(adminService.getById("999")).thenReturn(null);

    mockMvc.perform(get("/api/admin/readings/{id}", "999")).andExpect(status().isNotFound());
    verify(adminService, times(1)).getById("999");
  }
}