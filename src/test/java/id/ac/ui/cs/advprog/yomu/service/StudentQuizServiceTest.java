package id.ac.ui.cs.advprog.yomu.service;

import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitRequest;
import id.ac.ui.cs.advprog.yomu.dto.QuizSubmitResponse;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.repository.QuizRepository;
import id.ac.ui.cs.advprog.yomu.repository.UserProgressRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentQuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private UserProgressRepository userProgressRepository;

    @Mock
    private QuizService quizService;

    @InjectMocks
    private StudentQuizService studentQuizService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getQuizQuestionTest() {
        String userId = "user1";
        String readingId = "reading1";

        List<Question> questions = List.of(new Question(), new Question());

        when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
            .thenReturn(false);
        when(quizRepository.findByReadingId(readingId)).thenReturn(questions);

        List<Question> result = studentQuizService.getQuizQuestion(userId, readingId);

        assertEquals(2, result.size());
        verify(quizRepository).findByReadingId(readingId);
    }

    @Test
    void getQuizQuestionWhenQuizAlreadyCompleted() {
        String userId = "user1";
        String readingId = "reading1";

        when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
            .thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> studentQuizService.getQuizQuestion(userId, readingId));

        assertEquals("You've completed this quiz", exception.getMessage());
    }

    @Test
    void submitQuiz_shouldReturnCorrectResult() {
        String userId = "user1";
        String readingId = "reading1";

        Question q1 = new Question();
        q1.setId("q1");
        q1.setCorrectAnswer("A");

        Question q2 = new Question();
        q2.setId("q2");
        q2.setCorrectAnswer("B");

        List<Question> questions = List.of(q1, q2);

        Map<String, String> answers = new HashMap<>();
        answers.put("q1", "A");
        answers.put("q2", "B");

        QuizSubmitRequest request = new QuizSubmitRequest();
        request.setAnswers(answers);
        request.setTimeTakenSeconds(120);

        when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
            .thenReturn(false);

        when(quizRepository.findByReadingId(readingId)).thenReturn(questions);

        QuizSubmitResponse response = studentQuizService.submitQuiz(userId, readingId, request);

        assertEquals(100, response.getScore());
        assertEquals(1.0, response.getAccuracy());
        assertEquals(2, response.getTotalQuestions());
        assertEquals(2, response.getCorrectAnswers());
        assertEquals(120, response.getTimeTaken());

        assertTrue(response.getQuestionResults().get("q1"));
        assertTrue(response.getQuestionResults().get("q2"));

        verify(quizService).completeQuiz(userId, readingId, 100, 1.0);
    }

    @Test
    void submitQuizTest() {

        String userId = "user1";
        String readingId = "reading1";

        Question q1 = new Question();
        q1.setId("q1");
        q1.setCorrectAnswer("A");

        Question q2 = new Question();
        q2.setId("q2");
        q2.setCorrectAnswer("B");

        List<Question> questions = List.of(q1, q2);

        Map<String, String> answers = new HashMap<>();
        answers.put("q1", "A");
        answers.put("q2", "C");

        QuizSubmitRequest request = new QuizSubmitRequest();
        request.setAnswers(answers);
        request.setTimeTakenSeconds(60);

        when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
            .thenReturn(false);

        when(quizRepository.findByReadingId(readingId)).thenReturn(questions);

        QuizSubmitResponse response = studentQuizService.submitQuiz(userId, readingId, request);

        assertEquals(50, response.getScore());
        assertEquals(0.5, response.getAccuracy());
        assertEquals(1, response.getCorrectAnswers());

        assertTrue(response.getQuestionResults().get("q1"));
        assertFalse(response.getQuestionResults().get("q2"));

        verify(quizService).completeQuiz(userId, readingId, 50, 0.5);
    }

    @Test
    void submitQuizWhenQuizAlreadyCompleted() {
        String userId = "user1";
        String readingId = "reading1";

        when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
            .thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> studentQuizService.submitQuiz(userId, readingId,new QuizSubmitRequest()));

        assertEquals("You've completed this quiz", exception.getMessage());
    }

    @Test
    void submitQuizWhenRequestIsNull() {
        String userId = "user1";
        String readingId = "reading1";

        when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
            .thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> studentQuizService.submitQuiz(userId, readingId, null));

        assertEquals("Please choose the answers before submit the quiz", exception.getMessage());
    }

    @Test
    void submitQuizWhenAnswersAreNull() {
        String userId = "user1";
        String readingId = "reading1";

        QuizSubmitRequest request = new QuizSubmitRequest();
        request.setAnswers(null);

        when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
            .thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> studentQuizService.submitQuiz(userId, readingId, request));

        assertEquals("Please choose the answers before submit the quiz", exception.getMessage());
    }

    @Test
    void submitQuizWhenNoQuestionsAvailable() {
        String userId = "user1";
        String readingId = "reading1";

        QuizSubmitRequest request = new QuizSubmitRequest();
        request.setAnswers(new HashMap<>());

        when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
            .thenReturn(false);

        when(quizRepository.findByReadingId(readingId))
            .thenReturn(List.of());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> studentQuizService.submitQuiz(userId, readingId, request));

        assertEquals("No quiz available for this reading", exception.getMessage());
    }

    @Test
    void submitQuizIgnoreCaseAndWhitespace() {
        String userId = "user1";
        String readingId = "reading1";

        Question question = new Question();
        question.setId("q1");
        question.setCorrectAnswer("A");

        QuizSubmitRequest request = new QuizSubmitRequest();

        Map<String, String> answers = new HashMap<>();
        answers.put("q1", "  a  ");

        request.setAnswers(answers);

        when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
            .thenReturn(false);

        when(quizRepository.findByReadingId(readingId)).thenReturn(List.of(question));

        QuizSubmitResponse response = studentQuizService.submitQuiz(userId, readingId, request);

        assertEquals(100, response.getScore());
        assertTrue(response.getQuestionResults().get("q1"));
    }

    @Test
    void submitQuizTreatNullStudentAnswerAsIncorrect() {
        String userId = "user1";
        String readingId = "reading1";

        Question question = new Question();
        question.setId("q1");
        question.setCorrectAnswer("A");

        QuizSubmitRequest request = new QuizSubmitRequest();
        request.setAnswers(new HashMap<>());

        when(userProgressRepository.existsByUserIdAndReadingId(userId, readingId))
            .thenReturn(false);

        when(quizRepository.findByReadingId(readingId)).thenReturn(List.of(question));

        QuizSubmitResponse response = studentQuizService.submitQuiz(userId, readingId, request);

        assertEquals(0, response.getScore());
        assertFalse(response.getQuestionResults().get("q1"));
    }
}