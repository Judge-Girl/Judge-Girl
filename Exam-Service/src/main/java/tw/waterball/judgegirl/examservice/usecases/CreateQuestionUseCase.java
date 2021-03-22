package tw.waterball.judgegirl.examservice.usecases;

import lombok.Data;
import tw.waterball.judgegirl.entities.Question;
import tw.waterball.judgegirl.examservice.repositories.QuestionRepository;

import javax.inject.Named;

@Named
public class CreateQuestionUseCase {
    private final QuestionRepository questionRepository;

    public CreateQuestionUseCase(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public void execute(Request request, Presenter presenter) {
        Question question = new Question(request.examId, request.problemId, request.quota, request.score);
        presenter.setQuestion(questionRepository.save(question));
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
    }

}
