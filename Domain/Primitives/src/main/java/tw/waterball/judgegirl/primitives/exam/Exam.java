package tw.waterball.judgegirl.primitives.exam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tw.waterball.judgegirl.commons.utils.JSR380Utils;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.findFirst;
import static tw.waterball.judgegirl.primitives.date.DateProvider.now;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Exam {

    private Integer id = null;

    @NotBlank
    private String name;

    @NotNull
    private Date startTime;

    @NotNull
    private Date endTime;

    @NotNull
    private String description;

    private List<@Valid Question> questions = new ArrayList<>();

    private List<@Valid Examinee> examinees = new ArrayList<>();

    public Exam(String name, Date startTime, Date endTime, String description) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }

    public void validate() {
        JSR380Utils.validate(this);
        if (startTime.after(endTime)) {
            throw new IllegalStateException();
        }
    }

    public boolean isUpcoming() {
        return getStartTime().after(now());
    }

    public boolean isPast() {
        return getEndTime().before(now());
    }

    public boolean isCurrent() {
        Date now = now();
        return getStartTime().before(now) && getEndTime().after(now);
    }

    public Optional<Question> getQuestionByProblemId(int problemId) {
        return findFirst(questions, q -> q.getProblemId() == problemId);
    }

    public void updateQuestion(Question question) {
        if (question.getExamId() != this.getId()) {
            throw new IllegalArgumentException("Exam's id inconsistent.");
        }
        Question updatedQuestion = getQuestionByProblemId(question.getProblemId())
                .orElseThrow(() -> notFound(Question.class).id(question.getId()));

        updatedQuestion.setQuestionOrder(question.getQuestionOrder());
        updatedQuestion.setQuota(question.getQuota());
        updatedQuestion.setScore(question.getScore());
    }
}