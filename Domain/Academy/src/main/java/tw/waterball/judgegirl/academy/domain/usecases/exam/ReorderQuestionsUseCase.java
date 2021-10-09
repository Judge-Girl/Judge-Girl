package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.exam.Exam;

import javax.inject.Named;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class ReorderQuestionsUseCase extends AbstractExamUseCase {

    public ReorderQuestionsUseCase(ExamRepository examRepository) {
        super(examRepository);
    }

    public void execute(Request request) throws NotFoundException {
        Exam exam = findExam(request.examId);
        exam.reorderQuestions(request.reorders);
        examRepository.save(exam);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public int examId;
        public int[] reorders;
    }
}
