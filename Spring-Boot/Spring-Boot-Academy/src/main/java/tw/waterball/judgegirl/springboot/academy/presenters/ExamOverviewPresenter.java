package tw.waterball.judgegirl.springboot.academy.presenters;

import tw.waterball.judgegirl.academy.domain.usecases.exam.GetExamOverviewUseCase;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.Question;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.springboot.academy.view.ExamHome;
import tw.waterball.judgegirl.springboot.academy.view.ExamHome.QuestionItem;

import java.util.ArrayList;
import java.util.List;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.zipToList;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ExamOverviewPresenter implements GetExamOverviewUseCase.Presenter {
    private Exam exam;
    private final List<Problem> problems = new ArrayList<>();

    @Override
    public void showExam(Exam exam) {
        this.exam = exam;
    }

    @Override
    public void showQuestion(Question question, Problem problem) {
        problems.add(problem);
    }

    public ExamHome present() {
        List<QuestionItem> questionItems = aggregateQuestionOverviews();
        return ExamHome.builder()
                .id(exam.getId())
                .name(exam.getName())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .description(exam.getDescription())
                .questions(questionItems)
                .build();
    }

    private List<QuestionItem> aggregateQuestionOverviews() {
        return zipToList(exam.getQuestions(), problems,
                (q, p) -> QuestionItem.toViewModel(q, p));
    }

}
