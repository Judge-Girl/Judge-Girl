package tw.waterball.judgegirl.springboot.exam.view;

import lombok.*;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.zipToList;

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
        return ExamOverview.builder()
                .id(exam.getId())
                .name(exam.getName())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .description(exam.getDescription())
                .questionViews(zipToList(exam.getQuestions(), problemViews, QuestionOverview::toViewModel))
                .totalScore(exam.getQuestions().stream().mapToInt(question -> question.getScore()).sum())
                .build();
    }
}
