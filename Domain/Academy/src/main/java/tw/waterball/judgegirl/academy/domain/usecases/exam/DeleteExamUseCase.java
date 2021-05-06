package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;

import javax.inject.Named;

@Named
@AllArgsConstructor
public class DeleteExamUseCase {

    private final ExamRepository examRepository;

    public void execute(int examId) {
        examRepository.deleteExamById(examId);
    }
}
