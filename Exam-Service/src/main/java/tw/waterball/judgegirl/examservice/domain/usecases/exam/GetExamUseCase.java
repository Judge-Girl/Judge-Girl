package tw.waterball.judgegirl.examservice.domain.usecases.exam;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.exam.Exam;
import tw.waterball.judgegirl.examservice.domain.repositories.ExamRepository;

import javax.inject.Named;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
@AllArgsConstructor
public class GetExamUseCase {
    private final ExamRepository examRepository;

    public void execute(int examId, ExamPresenter presenter) {
        Exam exam = findExam(examId);
        presenter.showExam(exam);
    }

    private Exam findExam(int examId) {
        return examRepository.findById(examId).orElseThrow(NotFoundException::new);
    }

}
