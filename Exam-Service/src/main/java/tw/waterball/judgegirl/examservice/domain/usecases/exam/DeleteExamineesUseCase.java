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

import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

@Named
@AllArgsConstructor
public class DeleteExamineesUseCase {
    private final ExamRepository examRepository;
    private final StudentServiceDriver studentServiceDriver;

    public void execute(Request request) throws NotFoundException {
        Exam exam = findExam(request);
        List<Student> students = findStudents(request);
        deleteExaminees(exam, students);
    }


    private Exam findExam(Request request) throws NotFoundException {
        return examRepository.findById(request.examId).orElseThrow(NotFoundException::new);
    }

    private List<Student> findStudents(Request request) {
        return studentServiceDriver.getStudentsByEmails(request.emails);
    }

    private void deleteExaminees(Exam exam, List<Student> students) {
        examRepository.deleteExaminees(exam.getId(), mapToList(students, Student::getId));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        public int examId;
        public List<String> emails;
    }
}
