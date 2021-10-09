package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.Question;

import javax.inject.Named;

@Named
public class UpdateQuestionUseCase extends AbstractExamUseCase {

    public UpdateQuestionUseCase(ExamRepository examRepository) {
        super(examRepository);
    }

    public void execute(Request request) throws NotFoundException {
        Exam exam = findExam(request.examId);
        exam.updateQuestion(new Question(request.examId, request.problemId, request.quota, request.score, request.questionOrder));
        examRepository.save(exam);
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
