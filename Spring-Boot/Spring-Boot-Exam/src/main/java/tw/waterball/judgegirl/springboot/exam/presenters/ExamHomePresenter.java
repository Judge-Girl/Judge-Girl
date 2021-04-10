package tw.waterball.judgegirl.springboot.exam.presenters;

import tw.waterball.judgegirl.entities.exam.Exam;
import tw.waterball.judgegirl.entities.exam.Question;
import tw.waterball.judgegirl.entities.exam.Record;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.examservice.domain.usecases.exam.GetExamProgressOverviewUseCase;
import tw.waterball.judgegirl.springboot.exam.view.ExamHome;
import tw.waterball.judgegirl.springboot.exam.view.ExamHome.QuestionItem;

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
    public void showRemainingQuotaOfQuestion(Question question, int remainingQuota) {
        remainingQuotaMap.put(question.getId(), remainingQuota);
    }

    public ExamHome present() {
        List<QuestionItem> questionItems = aggregateQuestionOverviews(exam.getQuestions(), problems, remainingQuotaMap, bestRecords);
        return ExamHome.builder()
                .id(exam.getId())
                .name(exam.getName())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .description(exam.getDescription())
                .questions(questionItems)
                .totalScore(questionItems.stream().mapToInt(QuestionItem::getYourScore).sum())
                .build();
    }

    private static List<QuestionItem> aggregateQuestionOverviews(List<Question> questions, List<Problem> problems,
                                                                 Map<Question.Id, Integer> remainingQuotaMap, List<Record> bestRecords) {
        var bestRecordsMap = bestRecords.stream().collect(toMap(Record::getQuestionId, identity()));
        return zipToList(questions, problems, (q, p) -> QuestionItem.toViewModel(q, p,
                remainingQuotaMap.get(q.getId()), bestRecordsMap.get(q.getId())));
    }

}
