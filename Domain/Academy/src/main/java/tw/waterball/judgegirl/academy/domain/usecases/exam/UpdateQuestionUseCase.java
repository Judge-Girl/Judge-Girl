package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.Question;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;

import javax.inject.Named;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

@Named
@AllArgsConstructor
public class UpdateQuestionUseCase {
    private final ExamRepository examRepository;

    public void execute(Request request) throws NotFoundException {
        Exam exam = findExam(request);
        exam.updateQuestion(new Question(request.examId, request.problemId, request.quota, request.score, request.questionOrder));
        examRepository.save(exam);
    }

    private Exam findExam(Request request) {
        return examRepository.findById(request.examId)
                .orElseThrow(() -> notFound(Exam.class).id(request.examId));
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
