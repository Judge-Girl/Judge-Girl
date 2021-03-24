package tw.waterball.judgegirl.examservice.usecases;

import lombok.AllArgsConstructor;
import lombok.Data;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.entities.Question;
import tw.waterball.judgegirl.examservice.repositories.ExamRepository;
import tw.waterball.judgegirl.examservice.repositories.QuestionRepository;

import javax.inject.Named;

@Named
@AllArgsConstructor
public class CreateQuestionUseCase {
    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    // private final ProblemServiceDriver problemServiceDriver;

    public void execute(Request request, Presenter presenter) {
        Question question = new Question(request.examId, request.problemId, request.quota, request.score, request.questionOrder);
        Exam exam = examRepository.findById(request.examId).orElseThrow(NotFoundException::new);
        question = questionRepository.save(question);
        exam.getQuestions().add(question);
        exam = examRepository.save(exam);
        presenter.setQuestion(question);
    }

    public interface Presenter {
        void setQuestion(Question question);
    }

    @Data
    public static class Request {
        public int examId;
        public int problemId;
        public int quota;
        public int score;
        public int questionOrder;
    }

}
