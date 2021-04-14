package tw.waterball.judgegirl.examservice.domain.usecases.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.examservice.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.problemapi.clients.StudentServiceDriver;

import javax.inject.Named;
import java.util.List;

@Named
public class AddExamParticipationUseCase {
    private final ExamRepository examRepository;
    private final StudentServiceDriver studentServiceDriver;

    public AddExamParticipationUseCase(ExamRepository examRepository, StudentServiceDriver studentServiceDriver) {
        this.examRepository = examRepository;
        this.studentServiceDriver = studentServiceDriver;
    }

    public void execute(Request request, Presenter presenter) throws NotFoundException {
        examRepository.findById(request.examId).orElseThrow(NotFoundException::new);
        List<Student> students = studentServiceDriver.getStudentsByEmails(request.emails);
        for (Student student : students) {
            examRepository.addParticipation(request.examId, student.getId());
            request.emails.remove(student.getEmail());
        }
        presenter.setErrorEmails(request.emails);
    }

    public interface Presenter {
        void setErrorEmails(List<String> emails);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        public int examId;
        public List<String> emails;
    }

}
