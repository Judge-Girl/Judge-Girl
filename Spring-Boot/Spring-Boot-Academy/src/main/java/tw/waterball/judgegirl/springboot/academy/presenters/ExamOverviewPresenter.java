package tw.waterball.judgegirl.springboot.academy.presenters;

import tw.waterball.judgegirl.academy.domain.usecases.exam.GetExamOverviewUseCase;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.IpAddress;
import tw.waterball.judgegirl.primitives.exam.Question;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.springboot.academy.view.ExamOverview;
import tw.waterball.judgegirl.springboot.academy.view.ExamOverview.QuestionItem;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

/**
 * @author - c11037at@gmail.com (snowmancc)
 */
public class ExamOverviewPresenter implements GetExamOverviewUseCase.Presenter {
    private final ExamOverview.ExamOverviewBuilder examOverviewBuilder = ExamOverview.builder();

    @Override
    public void showExam(Exam exam) {
        this.examOverviewBuilder
                .id(exam.getId())
                .name(exam.getName())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .description(exam.getDescription())
                .whitelist(mapToList(exam.getWhitelist(), IpAddress::getIpAddress));
    }

    @Override
    public void showQuestion(Question question, Problem problem) {
        this.examOverviewBuilder.question(QuestionItem.toViewModel(problem, question));
    }

    @Override
    public void showNotFoundQuestion(Question question) {
        this.examOverviewBuilder.question(QuestionItem.toViewModel(question));
    }

    public ExamOverview present() {
        return this.examOverviewBuilder.build();
    }

}
