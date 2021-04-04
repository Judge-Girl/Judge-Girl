package tw.waterball.judgegirl.springboot.exam.repositories;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.entities.exam.Answer;
import tw.waterball.judgegirl.entities.exam.Exam;
import tw.waterball.judgegirl.entities.exam.Question;
import tw.waterball.judgegirl.examservice.domain.repositories.ExamFilter;
import tw.waterball.judgegirl.examservice.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.springboot.exam.repositories.jpa.*;
import tw.waterball.judgegirl.springboot.helpers.SkipAndSizePageable;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;
import static tw.waterball.judgegirl.entities.date.DateProvider.now;
import static tw.waterball.judgegirl.springboot.exam.repositories.jpa.ExamData.toData;
import static tw.waterball.judgegirl.springboot.exam.repositories.jpa.QuestionData.toData;

@Component
@Slf4j
@AllArgsConstructor
public class JpaExamRepository implements ExamRepository {
    private final JpaExamDataPort jpaExamDataPort;
    private final JpaQuestionDataPort jpaQuestionDataPort;
    private final JpaExamParticipationDataPort jpaExamParticipationDataPort;
    private final JpaAnswerDataPort jpaAnswerDataPort;

    @Override
    public Optional<Exam> findById(int examId) {
        return jpaExamDataPort.findById(examId)
                .map(ExamData::toEntity);
    }

    @Override
    public Optional<Question> findQuestion(Question.Id id) {
        return jpaQuestionDataPort.findById(new QuestionData.Id(id))
                .map(QuestionData::toEntity);
    }

    @Override
    public List<Question> findQuestionsInExam(int examId) {
        return mapToList(jpaQuestionDataPort.findById_ExamId(examId),
                QuestionData::toEntity);
    }

    @Override
    public List<Exam> findByIdIn(Collection<Integer> examIds) {
        return jpaExamDataPort.findByIdIn(examIds).stream().map(ExamData::toEntity).collect(toList());
    }

    @Override
    @Transactional
    public List<Exam> findExams(ExamFilter examFilter) {
        ExamFilter.Status status = examFilter.getStatus();
        Pageable pageable = new SkipAndSizePageable(examFilter.getSkip(), examFilter.getSize());

        try {
            return examFilter.getStudentId()
                    .map(studentId -> jpaExamDataPort.findStudentExams(studentId, status, now(), pageable))
                    .orElseGet(() -> jpaExamDataPort.findExams(status, now(), pageable))
                    .stream().map(ExamData::toEntity).collect(toList());
        } catch (RuntimeException err) {
            log.error("Error during exams filtering.", err);
            throw err;
        }
    }

    @Override
    public void addQuestion(Question question) {
        try {
            jpaQuestionDataPort.save(toData(question));
        } catch (Exception err) {
            // exam doesn't exist
            throw notFound("exam").id(question.getExamId());
        }
    }

    @Override
    public void addParticipation(int examId, int studentId) {
        ExamParticipationData data = jpaExamParticipationDataPort.save(
                new ExamParticipationData(examId, studentId));
        jpaExamParticipationDataPort.save(data);
    }

    @Override
    public void deleteQuestionById(Question.Id id) {
        try {
            jpaQuestionDataPort.deleteById(new QuestionData.Id(id.getExamId(), id.getProblemId()));
        } catch (EmptyResultDataAccessException err) {
            throw notFound("question").id(id);
        }
    }

    @Override
    public Exam save(Exam exam) {
        ExamData data = jpaExamDataPort.save(toData(exam));
        exam = data.toEntity();
        return exam;
    }

    @Override
    public void deleteAll() {
        jpaExamDataPort.deleteAll();
    }

    @Override
    public Answer saveAnswer(Answer answer) {
        AnswerData data = jpaAnswerDataPort.save(AnswerData.toData(answer));
        return data.toEntity();
    }

    @Override
    public Optional<Answer> findAnswer(Answer.Id id) {
        return jpaAnswerDataPort.findById(new AnswerData.Id(id))
                .map(AnswerData::toEntity);
    }

    @Override
    public int countAnswersInQuestion(Question.Id id, int studentId) {
        return jpaAnswerDataPort.countAllByExamIdAndProblemIdAndStudentId(id.getExamId(), id.getProblemId(), studentId);
    }

    @Override
    public boolean hasStudentParticipatedExam(int studentId, int examId) {
        return jpaExamParticipationDataPort.existsById_StudentIdAndId_ExamId(studentId, examId);
    }
}
