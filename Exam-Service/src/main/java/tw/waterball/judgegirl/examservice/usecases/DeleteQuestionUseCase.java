package tw.waterball.judgegirl.examservice.usecases;

import lombok.AllArgsConstructor;
import lombok.Value;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Question;
import tw.waterball.judgegirl.examservice.repositories.ExamRepository;

import javax.inject.Named;

@Named
@AllArgsConstructor
public class DeleteQuestionUseCase {
    private final ExamRepository examRepository;

    public void execute(Request request) throws NotFoundException {
        examRepository.deleteQuestionById(new Question.Id(request.examId, request.problemId));
    }

    @Value
    public static class Request {
        public int examId;
        public int problemId;
    }
}
