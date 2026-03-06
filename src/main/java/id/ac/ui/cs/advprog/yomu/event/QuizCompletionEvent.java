package id.ac.ui.cs.advprog.yomu.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

// berfungsi untuk mengirimkan notifikasi ke modul lain ketika pelajar menyelesaikan kuis
@Getter
public class QuizCompletionEvent extends ApplicationEvent {

  private final String userId;
  private final String readingId;

  public QuizCompletionEvent(Object source, String userId, String readingId) {
    super(source);

    if (userId == null || !userId.matches("^[a-zA-Z0-9]+$")) {
      throw new IllegalArgumentException("Invalid userId format: Contains malicious characters");
    }
    if (readingId == null || !readingId.matches("^[a-zA-Z0-9-]+$")) {
      throw new IllegalArgumentException("Invalid readingId format");
    }

    this.userId = userId;
    this.readingId = readingId;
  }
}
