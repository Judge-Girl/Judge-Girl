package tw.waterball.judgegirl.examservice.usecases;

import lombok.Value;
import tw.waterball.judgegirl.examservice.repositories.QuestionRepository;

import javax.inject.Named;

@Named
public class DeleteQuestionUseCase {
    private final QuestionRepository questionRepository;

    public DeleteQuestionUseCase(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public Boolean execute(Request request) {
        return questionRepository.deleteByIdAndExamId(request.questionId, request.examId) == 1;
    }

    @Value
    public static class Request {
        public int examId;
        public int questionId;
    }
}
