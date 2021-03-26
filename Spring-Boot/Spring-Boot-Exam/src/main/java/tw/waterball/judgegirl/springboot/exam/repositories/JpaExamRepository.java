package tw.waterball.judgegirl.springboot.exam.repositories;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.examservice.repositories.ExamRepository;
import tw.waterball.judgegirl.springboot.exam.repositories.jpa.ExamData;
import tw.waterball.judgegirl.springboot.exam.repositories.jpa.JpaExamDataPort;
import tw.waterball.judgegirl.springboot.exam.repositories.jpa.JpaQuestionDataPort;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static tw.waterball.judgegirl.springboot.exam.repositories.jpa.ExamData.toData;

@Component
@AllArgsConstructor
public class JpaExamRepository implements ExamRepository {

    private final JpaExamDataPort jpaExamDataPort;

    private final JpaQuestionRepository jpaQuestionRepository;

    @Override
    public Optional<Exam> findById(int examId) {
        return jpaExamDataPort.findById(examId).map(ExamData::toEntity);
    }

    @Override
    public List<Exam> findByIdIn(Collection<Integer> examIds) {
        return jpaExamDataPort.findByIdIn(examIds).stream().map(ExamData::toEntity).collect(toList());
    }

    @Override
    public void deleteQuestionById(int examId, int problemId) {
        try {
            jpaQuestionRepository.deleteByIdExamIdAndIdProblemId(examId, problemId);
        } catch (IllegalArgumentException  e) {
            throw new NotFoundException(e);
        }
    }

    @Override
    public Exam save(Exam exam) {
        ExamData data = jpaExamDataPort.save(toData(exam));
        exam = data.toEntity();
        return exam;
    }

    @Override
    public void deleteAll() {
        jpaExamDataPort.deleteAll();
    }
}
