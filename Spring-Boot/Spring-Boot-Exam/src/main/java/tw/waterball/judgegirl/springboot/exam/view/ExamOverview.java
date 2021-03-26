package tw.waterball.judgegirl.springboot.exam.view;

import lombok.*;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamOverview {
    private Integer id;
    private String name;
    private Date startTime;
    private Date endTime;
    private String description;
    private List<QuestionOverview> questionViews;
    private int totalScore;

    public static ExamOverview toViewModel(Exam exam, List<ProblemView> problemViews) {
        ExamOverview examOverview = ExamOverview.builder()
                .id(exam.getId())
                .name(exam.getName())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .description(exam.getDescription())
                .questionViews(new ArrayList<>())
                .totalScore(exam.getQuestions().stream().mapToInt(question -> question.getScore()).sum())
                .build();
        for (int i = 0; i < exam.getQuestions().size(); i++) {
            examOverview.questionViews.add(QuestionOverview.toViewModel(exam.getQuestions().get(i), problemViews.get(i)));
        }
        return examOverview;
    }
}
