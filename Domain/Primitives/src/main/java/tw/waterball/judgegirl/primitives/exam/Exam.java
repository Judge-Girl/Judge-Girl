package tw.waterball.judgegirl.primitives.exam;

import lombok.Getter;
import tw.waterball.judgegirl.primitives.time.Duration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;
import static tw.waterball.judgegirl.commons.utils.JSR380Utils.validate;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.findFirst;
import static tw.waterball.judgegirl.primitives.time.Duration.during;

@Getter
public class Exam {

    private final Integer id;

    @NotBlank
    private String name;

    @NotNull
    private Duration duration;

    @NotNull
    private String description;

    private final List<Question> questions;

    private final List<Examinee> examinees;

    public Exam(String name, Duration duration, String description) {
        this(null, name, duration, description);
    }

    public Exam(Integer id, String name, Duration duration, String description) {
        this(id, name, duration, description, new ArrayList<>(), new ArrayList<>());
    }

    public Exam(String name, Duration duration, String description,
                List<Question> questions, List<Examinee> examinees) {
        this(null, name, duration, description, questions, examinees);
    }

    public Exam(Integer id, String name, Duration duration, String description,
                List<Question> questions, List<Examinee> examinees) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.description = description;
        this.questions = questions;
        this.examinees = examinees;
        validate(this);
    }

    public boolean isUpcoming() {
        return getDuration().isUpcoming();
    }

    public boolean isPast() {
        return getDuration().isPast();
    }

    public boolean isCurrent() {
        return getDuration().isOngoing();
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

    public void addQuestion(Question question) {
        questions.add(question);
    }

    public void addExaminee(Examinee examinee) {
        examinees.add(examinee);
    }

    public void foreachQuestion(Consumer<Question> questionConsumer) {
        questions.forEach(questionConsumer);
    }

    public void setName(String name) {
        this.name = requireNonNull(name);
    }

    public void setDescription(String description) {
        this.description = requireNonNull(description);
    }

    public Date getStartTime() {
        return getDuration().getStartTime();
    }

    public Date getEndTime() {
        return getDuration().getEndTime();
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(Date startTime) {
        setDuration(during(startTime, getEndTime()));
    }

    public void setEndTime(Date endTime) {
        setDuration(during(getStartTime(), endTime));
    }

    public Optional<Examinee> getExaminee(Examinee.Id examineeId) {
        return findFirst(getExaminees(), examinee -> examinee.getId().equals(examineeId));
    }
}