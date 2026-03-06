package id.ac.ui.cs.advprog.yomu.controller;

import id.ac.ui.cs.advprog.yomu.entity.Reading;
import id.ac.ui.cs.advprog.yomu.service.AdminReadingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminReadingControllerTest {

  private AdminReadingService adminService;
  private AdminReadingController controller;

  @BeforeEach
  void setUp() {
    adminService = mock(AdminReadingService.class);
    controller = new AdminReadingController(adminService);
  }

  @Test
  void testCreateReading() {
    Reading reading = new Reading();
    reading.setTitle("Title 1");

    when(adminService.createReading(reading)).thenReturn(reading);

    ResponseEntity<Reading> response = controller.create(reading);

    assertEquals(reading, response.getBody());
    verify(adminService, times(1)).createReading(reading);
  }

  @Test
  void testGetAllReadings() {
    Reading r1 = new Reading();
    r1.setTitle("R1");
    Reading r2 = new Reading();
    r2.setTitle("R2");

    List<Reading> readings = Arrays.asList(r1, r2);
    when(adminService.findAll()).thenReturn(readings);

    ResponseEntity<List<Reading>> response = controller.getAll();

    assertEquals(2, response.getBody().size());
    assertEquals("R1", response.getBody().get(0).getTitle());
    verify(adminService, times(1)).findAll();
  }

  @Test
  void testUpdateReading() {
    String id = "reading1";
    Reading updatedReading = new Reading();
    updatedReading.setTitle("Updated");

    ResponseEntity<Void> response = controller.update(id, updatedReading);

    assertEquals(200, response.getStatusCodeValue());
    verify(adminService, times(1)).
        updateReading(id, updatedReading);
  }

  @Test
  void testDeleteReading() {
    String id = "reading1";

    ResponseEntity<Void> response = controller.delete(id);

    assertEquals(200, response.getStatusCodeValue());
    verify(adminService, times(1)).
        deleteReading(id);
  }
}