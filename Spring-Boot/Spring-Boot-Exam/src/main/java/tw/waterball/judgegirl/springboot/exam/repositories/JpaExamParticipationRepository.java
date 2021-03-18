package tw.waterball.judgegirl.springboot.exam.repositories;

import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.entities.ExamParticipation;
import tw.waterball.judgegirl.examservice.repositories.ExamParticipationRepository;
import tw.waterball.judgegirl.springboot.exam.repositories.jpa.ExamParticipationData;
import tw.waterball.judgegirl.springboot.exam.repositories.jpa.JpaExamParticipationDataPort;

import java.util.List;
import java.util.stream.Collectors;

import static tw.waterball.judgegirl.springboot.exam.repositories.jpa.ExamParticipationData.toData;

@Component
public class JpaExamParticipationRepository implements ExamParticipationRepository {

    public final JpaExamParticipationDataPort jpaExamParticipationDataPort;

    public JpaExamParticipationRepository(JpaExamParticipationDataPort jpaExamParticipationDataPort) {
        this.jpaExamParticipationDataPort = jpaExamParticipationDataPort;
    }

    @Override
    public List<ExamParticipation> findByStudentId(int studentId) {
        return jpaExamParticipationDataPort.findByStudentId(studentId).stream().map(ExamParticipationData::toEntity).collect(Collectors.toList());
    }

    @Override
    public ExamParticipation save(ExamParticipation examParticipation) {
        ExamParticipationData data = jpaExamParticipationDataPort.save(toData(examParticipation));
        examParticipation.setId(data.getId());
        return examParticipation;
    }

    @Override
    public void deleteAll() {
        jpaExamParticipationDataPort.deleteAll();
    }
}
