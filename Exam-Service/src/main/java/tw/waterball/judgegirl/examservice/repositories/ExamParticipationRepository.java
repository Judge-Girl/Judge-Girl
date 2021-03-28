package tw.waterball.judgegirl.examservice.repositories;

import tw.waterball.judgegirl.entities.ExamParticipation;

import java.util.List;

public interface ExamParticipationRepository {

    List<ExamParticipation> findByStudentId(int studentId);

    ExamParticipation save(ExamParticipation examParticipation);

    void deleteAll();
}
