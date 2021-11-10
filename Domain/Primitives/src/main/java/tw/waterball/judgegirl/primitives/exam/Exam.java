package tw.waterball.judgegirl.primitives.exam;

import lombok.Getter;
import tw.waterball.judgegirl.primitives.time.Duration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;
import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.findFirst;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.sum;
import static tw.waterball.judgegirl.commons.utils.validations.ValidationUtils.validate;
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

    private List<IpAddress> whitelist;

    public Exam(String name, Duration duration, String description) {
        this(null, name, duration, description);
    }

    public Exam(Integer id, String name, Duration duration, String description) {
        this(id, name, duration, description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public Exam(String name, Duration duration, String description,
                List<Question> questions, List<Examinee> examinees, List<IpAddress> whitelist) {
        this(null, name, duration, description, questions, examinees, whitelist);
    }

    public Exam(Integer id, String name, Duration duration, String description,
                List<Question> questions, List<Examinee> examinees, List<IpAddress> whitelist) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.description = description;
        this.questions = questions;
        this.examinees = examinees;
        this.whitelist = whitelist;
        validate(this);
    }

    public boolean isUpcoming() {
        return getDuration().isUpcoming();
    }

    public boolean isPast() {
        return getDuration().isPast();
    }

    public boolean isOngoing() {
        return getDuration().isOngoing();
    }

    public Question getQuestionById(Question.Id questionId) {
        return mayContainQuestion(questionId).orElseThrow(() -> notFound(Question.class).id(questionId));
    }

    public boolean containQuestion(int problemId) {
        return mayContainQuestion(problemId).isPresent();
    }

    public Optional<Question> mayContainQuestion(int problemId) {
        return findFirst(questions, q -> q.getId().getProblemId() == problemId);
    }

    public Optional<Question> mayContainQuestion(Question.Id questionId) {
        return findFirst(questions, q -> q.getId().equals(questionId));
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

    public void reorderQuestions(int... reorders) {
        if (questions.size() != reorders.length) {
            throw new IllegalArgumentException("Reorders amount should be same as questions amount");
        }
        for (int index = 0; index < questions.size(); index++) {
            questions.get(index).setQuestionOrder(reorders[index]);
        }
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }

    public void addExaminee(Examinee examinee) {
        examinees.add(examinee);
    }

    public int getMaxScore() {
        return sum(questions, Question::getScore);
    }

    public void setName(String name) {
        this.name = requireNonNull(name);
    }

    public void setDescription(String description) {
        this.description = requireNonNull(description);
    }

    public void setWhitelist(List<IpAddress> whitelist) {
        this.whitelist = requireNonNullElseGet(whitelist, this::getWhitelist);
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasExaminee(int studentId) {
        return getExaminee(studentId).isPresent();
    }

    public Optional<Examinee> getExaminee(int studentId) {
        return findFirst(getExaminees(), examinee -> examinee.getId().getStudentId() == studentId);
    }
}