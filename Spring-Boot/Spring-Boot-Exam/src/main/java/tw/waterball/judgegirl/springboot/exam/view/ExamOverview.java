package tw.waterball.judgegirl.springboot.exam.view;

import lombok.*;
import tw.waterball.judgegirl.entities.Exam;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    public static ExamOverview toViewModel(Exam exam) {
        return ExamOverview.builder()
                .id(exam.getId())
                .name(exam.getName())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .description(exam.getDescription())
                .questionViews(exam.getQuestions().stream().map(QuestionOverview::toViewModel).collect(Collectors.toList()))
                .totalScore(exam.getQuestions().stream().mapToInt(question -> question.getScore()).sum())
                .build();
    }
}
