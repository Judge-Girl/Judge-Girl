package tw.waterball.judgegirl.studentservice.domain.usecases.student;

import javax.inject.Named;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

@Named
@AllArgsConstructor
public class DeleteStudentUseCase {

    private final StudentRepository studentRepository;

    public void execute(int studentId) {
        studentRepository.deleteStudentById(studentId);
    }
}
