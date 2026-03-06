package id.ac.ui.cs.advprog.yomu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.dto.ReadingRequestDTO;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminReadingControllerTest {

  private MockMvc mockMvc;

  @Mock
  private AdminReadingService adminService;

  @InjectMocks
  private AdminReadingController controller;

  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void testCreate() throws Exception {
    ReadingRequestDTO dto = new ReadingRequestDTO();
    dto.setTitle("Test Title");
    dto.setContent("Test Content");
    dto.setCategory("Science");
    dto.setDifficultyLevel("Medium");

    Reading savedReading = new Reading();
    savedReading.setTitle(dto.getTitle());
    savedReading.setContent(dto.getContent());
    savedReading.setCategory(dto.getCategory());
    savedReading.setDifficultyLevel(dto.getDifficultyLevel());

    when(adminService.createReading(any(ReadingRequestDTO.class))).thenReturn(savedReading);

    mockMvc.perform(post("/api/admin/readings/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Test Title"))
        .andExpect(jsonPath("$.content").value("Test Content"))
        .andExpect(jsonPath("$.category").value("Science"))
        .andExpect(jsonPath("$.difficultyLevel").value("Medium"));

    verify(adminService, times(1)).createReading(any(ReadingRequestDTO.class));
  }

  @Test
  void testGetAll() throws Exception {
    Reading r1 = new Reading();
    r1.setTitle("R1");
    Reading r2 = new Reading();
    r2.setTitle("R2");

    List<Reading> readings = Arrays.asList(r1, r2);
    when(adminService.findAll()).thenReturn(readings);

    mockMvc.perform(get("/api/admin/readings"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].title").value("R1"))
        .andExpect(jsonPath("$[1].title").value("R2"));

    verify(adminService, times(1)).findAll();
  }

  @Test
  void testUpdate() throws Exception {
    String id = "123";
    ReadingRequestDTO dto = new ReadingRequestDTO();
    dto.setTitle("Updated Title");
    dto.setContent("Updated Content");
    dto.setCategory("Math");
    dto.setDifficultyLevel("Hard");

    doNothing().when(adminService).updateReading(eq(id), any(ReadingRequestDTO.class));

    mockMvc.perform(put("/api/admin/readings/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk());

    verify(adminService, times(1)).updateReading(eq(id), any(ReadingRequestDTO.class));
  }

  @Test
  void testDelete() throws Exception {
    String id = "123";
    doNothing().when(adminService).deleteReading(id);

    mockMvc.perform(delete("/api/admin/readings/{id}", id))
        .andExpect(status().isOk());

    verify(adminService, times(1)).deleteReading(id);
  }
}