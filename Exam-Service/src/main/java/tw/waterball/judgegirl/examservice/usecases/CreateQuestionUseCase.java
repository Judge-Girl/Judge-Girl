package tw.waterball.judgegirl.examservice.usecases;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.entities.Question;
import tw.waterball.judgegirl.examservice.repositories.ExamRepository;
import tw.waterball.judgegirl.examservice.repositories.QuestionRepository;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

import javax.inject.Named;

@Named
@AllArgsConstructor
public class CreateQuestionUseCase {
    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final ProblemServiceDriver problemServiceDriver;

    public void execute(Request request, Presenter presenter) throws NotFoundException {
        Question question = new Question(request.examId, request.problemId, request.quota, request.score, request.questionOrder);
        Exam exam = examRepository.findById(request.examId).orElseThrow(NotFoundException::new);
        ProblemView problem = problemServiceDriver.getProblem(request.problemId);
        question = questionRepository.save(question);
        exam.getQuestions().add(question);
        exam = examRepository.save(exam);
        presenter.setQuestion(question);
    }

    public interface Presenter {
        void setQuestion(Question question);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public int examId;
        public int problemId;
        public int quota;
        public int score;
        public int questionOrder;
    }

}
