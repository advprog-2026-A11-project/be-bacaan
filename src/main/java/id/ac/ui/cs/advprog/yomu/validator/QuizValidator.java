package id.ac.ui.cs.advprog.yomu.validator;

import id.ac.ui.cs.advprog.yomu.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.entity.Question;
import id.ac.ui.cs.advprog.yomu.entity.Reading;

public interface QuizValidator {
  void validate(QuizQuestionRequest request);
  Question createQuestion(QuizQuestionRequest request, Reading reading);
  void updateQuestion(Question question, QuizQuestionRequest request);
  String getQuestionType();

}
