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
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class UpdateQuestionsUseCase extends AbstractExamUseCase {

    public UpdateQuestionsUseCase(ExamRepository examRepository) {
        super(examRepository);
    }

    public void execute(Request request) throws NotFoundException {
        Exam exam = findExam(request.examId);
        request.updateQuestion(exam);
        examRepository.save(exam);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public int examId;
        public List<QuestionUpsert> questions;

        public Request(int examId, QuestionUpsert questionUpsert) {
            this(examId, of(questionUpsert));
        }

        public void updateQuestion(Exam exam) {
            mapToList(questions, QuestionUpsert::toValue)
                    .forEach(exam::updateQuestion);
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionUpsert {
        public int examId;
        public int problemId;
        public int quota;
        public int score;
        public int questionOrder;

        public Question toValue() {
            return new Question(examId, problemId, quota, score, questionOrder);
        }
    }
}
