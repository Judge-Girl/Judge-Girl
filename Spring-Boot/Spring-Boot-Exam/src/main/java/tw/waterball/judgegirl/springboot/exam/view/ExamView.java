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
public class ExamView {
    private Integer id;
    private String name;
    private Date startTime;
    private Date endTime;
    private List<QuestionView> questionViews;

    public static ExamView toViewModel(Exam exam) {
        return ExamView.builder()
                .id(exam.getId())
                .name(exam.getName())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .questionViews(exam.getQuestions().stream().map(QuestionView::toViewModel).collect(Collectors.toList()))
                .build();
    }
}
