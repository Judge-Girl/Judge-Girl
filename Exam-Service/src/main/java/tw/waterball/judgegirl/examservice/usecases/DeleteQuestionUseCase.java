package tw.waterball.judgegirl.examservice.usecases;

import lombok.AllArgsConstructor;
import lombok.Value;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.examservice.repositories.ExamRepository;
import tw.waterball.judgegirl.examservice.repositories.QuestionRepository;

import javax.inject.Named;

@Named
@AllArgsConstructor
public class DeleteQuestionUseCase {
    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;

    public void execute(Request request) throws NotFoundException {
        Exam exam = examRepository.findById(request.examId).orElseThrow(NotFoundException::new);
        if (!exam.getQuestions().removeIf(question -> question.getId() == request.questionId && question.getExamId() == request.examId)) {
            throw new NotFoundException();
        }
        examRepository.save(exam);
    }

    @Value
    public static class Request {
        public int examId;
        public int questionId;
    }
}
