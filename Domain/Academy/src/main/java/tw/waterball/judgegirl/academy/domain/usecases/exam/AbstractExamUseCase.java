package tw.waterball.judgegirl.academy.domain.usecases.exam;

import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.exam.Exam;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public abstract class AbstractExamUseCase {
    protected final ExamRepository examRepository;

    public AbstractExamUseCase(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    protected Exam findExam(int examId) throws NotFoundException {
        return examRepository.findById(examId)
                .orElseThrow(() -> notFound(Exam.class).id(examId));
    }

}
