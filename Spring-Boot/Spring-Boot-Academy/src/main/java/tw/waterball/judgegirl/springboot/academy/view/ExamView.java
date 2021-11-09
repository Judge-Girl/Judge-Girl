package tw.waterball.judgegirl.springboot.academy.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.IpAddress;

import java.util.Date;
import java.util.List;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExamView {
    public int id;
    public String name;
    public Date startTime;
    public Date endTime;
    public String description;
    public List<QuestionView> questions;
    private List<String> whitelist;

    public static ExamView toViewModel(Exam exam) {
        return ExamView.builder()
                .id(exam.getId())
                .name(exam.getName())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .description(exam.getDescription())
                .questions(mapToList(exam.getQuestions(), QuestionView::toViewModel))
                .whitelist(mapToList(exam.getWhitelist(), IpAddress::getIpAddress))
                .build();
    }
}
