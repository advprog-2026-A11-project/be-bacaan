package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.entity.TextContext;
import id.ac.ui.cs.advprog.yomu.repository.TextContextRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TextContextServiceImplTest {

  @Mock
  TextContextRepository repository;

  @InjectMocks
  TextContextServiceImpl service;

  TextContext text;

  @BeforeEach
  void setup() {

  }

  @Test
  void testFindAll() {
    TextContext text = new TextContext();
    text.setId("1");
    text.setTitle("Example");

    List<TextContext> list = List.of(text);
    when(repository.findAll()).thenReturn(list);

    List<TextContext> result = service.findAll();
    assertEquals(1, result.size());
  }

  @Test
  void testFindById() {
    TextContext text = new TextContext();
    text.setId("1");
    text.setTitle("Example");

    when(repository.findById("1")).thenReturn(Optional.of(text));

    Optional<TextContext> result = service.findById("1");
    assertTrue((result.isPresent()));
  }

  @Test
  void testFindById_notFound() {
    when(repository.findById("100")).thenReturn((Optional.empty()));

    Optional<TextContext> result = service.findById("100");

    assertFalse(result.isPresent());
  }
}
