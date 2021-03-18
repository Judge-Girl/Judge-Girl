package tw.waterball.judgegirl.springboot.exam.repositories;

import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.examservice.repositories.ExamRepository;
import tw.waterball.judgegirl.springboot.exam.repositories.jpa.ExamData;
import tw.waterball.judgegirl.springboot.exam.repositories.jpa.JpaExamDataPort;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static tw.waterball.judgegirl.springboot.exam.repositories.jpa.ExamData.toData;

@Component
public class JpaExamRepository implements ExamRepository {

    private final JpaExamDataPort jpaExamDataPort;

    public JpaExamRepository(JpaExamDataPort jpaExamDataPort) {
        this.jpaExamDataPort = jpaExamDataPort;
    }

    @Override
    public List<Exam> findByIdIn(Collection<Integer> examIds) {
        return jpaExamDataPort.findByIdIn(examIds).stream().map(ExamData::toEntity).collect(toList());
    }

    @Override
    public Exam save(Exam exam) {
        ExamData data = jpaExamDataPort.save(toData(exam));
        exam.setId(data.getId());
        return exam;
    }

    @Override
    public void deleteAll() {
        jpaExamDataPort.deleteAll();
    }
}
