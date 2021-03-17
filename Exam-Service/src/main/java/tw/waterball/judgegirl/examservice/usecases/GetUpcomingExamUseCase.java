package tw.waterball.judgegirl.examservice.usecases;

import lombok.Value;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.entities.ExamParticipation;
import tw.waterball.judgegirl.examservice.repositories.ExamParticipationRepository;
import tw.waterball.judgegirl.examservice.repositories.ExamRepository;

import javax.inject.Named;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Named
public class GetUpcomingExamUseCase {
    private final ExamRepository examRepository;
    private final ExamParticipationRepository examParticipationRepository;

    public GetUpcomingExamUseCase(ExamRepository examRepository, ExamParticipationRepository examParticipationRepository) {
        this.examRepository = examRepository;
        this.examParticipationRepository = examParticipationRepository;
    }

    public void execute(Request request, Presenter presenter) {
        Date now = new Date();
        List<Exam> examList = findUpcomingExams(request.studentId);
        presenter.setExams(examList.stream()
                .filter(exam -> exam.getStartTime().after(now))
                .collect(toList()));
    }

    private List<Exam> findUpcomingExams(Integer studentId) {
        List<Integer> examIds = examParticipationRepository.findByStudentId(studentId).stream()
                .map(ExamParticipation::getExamId).collect(toList());
        return examRepository.findByIdIn(examIds);
    }

    public interface Presenter {
        void setExams(List<Exam> exams);
    }

    @Value
    public static class Request {
        @NotNull
        public int studentId;
    }

}
