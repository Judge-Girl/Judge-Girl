package tw.waterball.judgegirl.examservice.domain.usecases.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.entities.exam.Exam;
import tw.waterball.judgegirl.examservice.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.studentapi.clients.StudentServiceDriver;

import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named
@AllArgsConstructor
public class AddExamineesUseCase {
    private final ExamRepository examRepository;
    private final StudentServiceDriver studentServiceDriver;

    public void execute(Request request, Presenter presenter) throws NotFoundException {
        Exam exam = findExam(request);
        List<Student> students = findStudents(request);
        addExaminees(exam, students);
        showNotFoundStudents(request, students, presenter);
    }

    private Exam findExam(Request request) throws NotFoundException {
        return examRepository.findById(request.examId).orElseThrow(NotFoundException::new);
    }

    private List<Student> findStudents(Request request) {
        return studentServiceDriver.getStudentsByEmails(request.emails);
    }

    private void addExaminees(Exam exam, List<Student> students) {
        examRepository.addExaminees(exam.getId(), students.stream().map(Student::getId).collect(Collectors.toList()));
    }

    private void showNotFoundStudents(Request request, List<Student> students, Presenter presenter) {
        List<String> foundEmails = students.stream().map(Student::getEmail).collect(Collectors.toList());
        List<String> notFoundEmails = request.emails.stream().filter(email -> !foundEmails.contains(email)).collect(Collectors.toList());
        presenter.showNotFoundEmails(notFoundEmails);
    }

    public interface Presenter {
        void showNotFoundEmails(List<String> emails);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        public int examId;
        public List<String> emails;
    }

}
