package tw.waterball.judgegirl.springboot.exam.repositories;

import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Question;
import tw.waterball.judgegirl.examservice.repositories.QuestionRepository;
import tw.waterball.judgegirl.springboot.exam.repositories.jpa.JpaQuestionDataPort;
import tw.waterball.judgegirl.springboot.exam.repositories.jpa.QuestionData;

import static tw.waterball.judgegirl.springboot.exam.repositories.jpa.QuestionData.toData;

@Component
public class JpaQuestionRepository implements QuestionRepository {

    private final JpaQuestionDataPort jpaQuestionDataPort;

    public JpaQuestionRepository(JpaQuestionDataPort jpaQuestionDataPort) {
        this.jpaQuestionDataPort = jpaQuestionDataPort;
    }

    public void deleteByIdExamIdAndIdProblemId(int examId, int problemId) {
        if (jpaQuestionDataPort.deleteByIdExamIdAndIdProblemId(examId, problemId) != 1) {
            throw new NotFoundException();
        }
    }

    @Override
    public Question save(Question question) {
        QuestionData data = jpaQuestionDataPort.save(toData(question));
        question = data.toEntity();
        return question;
    }

    @Override
    public void deleteAll() {
        jpaQuestionDataPort.deleteAll();
    }
}
