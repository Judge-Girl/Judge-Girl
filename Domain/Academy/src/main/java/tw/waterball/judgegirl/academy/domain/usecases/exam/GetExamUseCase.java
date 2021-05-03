package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;

import javax.inject.Named;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

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
        return examRepository.findById(examId)
                .orElseThrow(() -> notFound(Exam.class).id(examId));
    }

}
