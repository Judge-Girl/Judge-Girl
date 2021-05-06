package tw.waterball.judgegirl.springboot.academy.presenters;

import tw.waterball.judgegirl.academy.domain.usecases.exam.GetExamProgressOverviewUseCase;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.Question;
import tw.waterball.judgegirl.primitives.exam.Record;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.springboot.academy.view.ExamHome;
import tw.waterball.judgegirl.springboot.academy.view.ExamHome.QuestionItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.zipToList;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ExamHomePresenter implements GetExamProgressOverviewUseCase.Presenter {
    private Exam exam;
    private final List<Record> bestRecords = new ArrayList<>();
    private final List<Problem> problems = new ArrayList<>();
    private final Map<Question.Id, Integer> yourScoreMap = new HashMap<>();
    private final Map<Question.Id, Integer> remainingQuotaMap = new HashMap<>();

    @Override
    public void showExam(Exam exam) {
        this.exam = exam;
    }

    @Override
    public void showQuestion(Question question, Problem problem) {
        problems.add(problem);
    }

    @Override
    public void showBestRecordOfQuestion(Record bestRecord) {
        bestRecords.add(bestRecord);
    }

    @Override
    public void showYourScoreOfQuestion(Question question, int yourScore) {
        yourScoreMap.put(question.getId(), yourScore);
    }

    @Override
    public void showRemainingQuotaOfQuestion(Question question, int remainingQuota) {
        remainingQuotaMap.put(question.getId(), remainingQuota);
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
                .totalScore(questionItems.stream().mapToInt(ExamHome.QuestionItem::getYourScore).sum())
                .build();
    }

    private List<QuestionItem> aggregateQuestionOverviews() {
        var bestRecordsMap = bestRecords.stream().collect(toMap(Record::getQuestionId, identity()));
        return zipToList(exam.getQuestions(), problems,
                (q, p) -> QuestionItem.toViewModel(q, p,
                        remainingQuotaMap.get(q.getId()), yourScoreMap.get(q.getId()), bestRecordsMap.get(q.getId())));
    }

}
