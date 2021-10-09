package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.Question;

import javax.inject.Named;
import java.util.List;

import static java.util.List.of;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class UpdateMultipleQuestionOrdersUseCase extends AbstractExamUseCase {

    public UpdateMultipleQuestionOrdersUseCase(ExamRepository examRepository) {
        super(examRepository);
    }

    public void execute(Request request) throws NotFoundException {
        Exam exam = findExam(request.examId);
        request.updateQuestionOrders(exam);
        examRepository.save(exam);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public int examId;
        public List<QuestionOrderUpsert> questions;

        public Request(int examId, QuestionOrderUpsert... questionOrderUpserts) {
            this(examId, of(questionOrderUpserts));
        }

        public void updateQuestionOrders(Exam exam) {
            questions.forEach(question -> question.updateQuestionOrder(exam));
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionOrderUpsert {
        public int examId;
        public int problemId;
        public int questionOrder;

        public void updateQuestionOrder(Exam exam) {
            Question question = exam.getQuestionById(new Question.Id(examId, problemId));
            question.setQuestionOrder(questionOrder);
            exam.updateQuestion(question);
        }
    }
}
