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
    this.userId = userId;
    this.readingId = readingId;
  }
}
