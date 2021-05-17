package tw.waterball.judgegirl.springboot.academy.repositories;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.academy.domain.repositories.ExamFilter;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.primitives.exam.*;
import tw.waterball.judgegirl.springboot.academy.repositories.jpa.*;
import tw.waterball.judgegirl.springboot.helpers.SkipAndSizePageable;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;
import static tw.waterball.judgegirl.primitives.time.DateProvider.now;
import static tw.waterball.judgegirl.springboot.academy.repositories.jpa.ExamData.toData;
import static tw.waterball.judgegirl.springboot.academy.repositories.jpa.QuestionData.toData;

@Component
@Slf4j
@AllArgsConstructor
public class JpaExamRepository implements ExamRepository {
    private final JpaExamDAO jpaExamDAO;
    private final JpaQuestionDAO jpaQuestionDAO;
    private final JpaExamineeDAO jpaExamineeDAO;
    private final JpaAnswerDAO jpaAnswerDAO;
    private final JpaBestRecordDAO jpaBestRecordDAO;

    @Override
    public Optional<Exam> findById(int examId) {
        return jpaExamDAO.findById(examId)
                .map(ExamData::toEntity);
    }

    @Override
    public Optional<Question> findQuestion(Question.Id id) {
        return jpaQuestionDAO.findById(new QuestionData.Id(id))
                .map(QuestionData::toEntity);
    }

    @Override
    public List<Question> findQuestionsInExam(int examId) {
        return mapToList(jpaQuestionDAO.findById_ExamId(examId),
                QuestionData::toEntity);
    }

    @Override
    public List<Exam> findByIdIn(Collection<Integer> examIds) {
        return mapToList(jpaExamDAO.findByIdIn(examIds), ExamData::toEntity);
    }

    @Override
    @Transactional
    public List<Exam> findExams(ExamFilter examFilter) {
        ExamFilter.Status status = examFilter.getStatus();
        Pageable pageable = new SkipAndSizePageable(examFilter.getSkip(), examFilter.getSize());

        try {
            return examFilter.getStudentId()
                    .map(studentId -> jpaExamDAO.findStudentExams(studentId, status, now(), pageable))
                    .orElseGet(() -> jpaExamDAO.findExams(status, now(), pageable))
                    .stream().map(ExamData::toEntity).collect(toList());
        } catch (RuntimeException err) {
            log.error("Error during exams filtering.", err);
            throw err;
        }
    }

    @Override
    public void saveBestRecordOfQuestion(Record record) {
        jpaBestRecordDAO.saveAndFlush(BestRecordData.toData(record));
    }

    @Override
    public Optional<Record> findBestRecordOfQuestion(Question.Id questionId, int studentId) {
        return jpaBestRecordDAO.findById(new BestRecordData.Id(new QuestionData.Id(questionId), studentId))
                .map(BestRecordData::toEntity);
    }

    @Override
    public void addQuestion(Question question) {
        try {
            jpaQuestionDAO.saveAndFlush(toData(question));
        } catch (Exception err) {
            // exam doesn't exist
            throw notFound(Exam.class).id(question.getExamId());
        }
    }

    @Override
    public void addExaminee(int examId, int studentId) {
        jpaExamineeDAO.saveAndFlush(new ExamineeData(examId, studentId));
    }

    @Override
    public void deleteExaminee(Examinee.Id id) {
        jpaExamineeDAO.deleteById(new ExamineeData.Id(id));
    }

    @Override
    public void addExaminees(int examId, List<Integer> studentIds) {
        jpaExamineeDAO.saveAll(mapToList(studentIds, studentId -> new ExamineeData(examId, studentId)));
    }

    @Override
    public void deleteExaminees(int examId, List<Integer> studentIds) {
        jpaExamineeDAO.deleteInBatch(mapToList(studentIds, studentId -> new ExamineeData(examId, studentId)));
    }

    @Override
    public void deleteQuestionById(Question.Id id) {
        try {
            jpaQuestionDAO.deleteById(new QuestionData.Id(id.getExamId(), id.getProblemId()));
        } catch (EmptyResultDataAccessException err) {
            throw notFound(Question.class).id(id);
        }
    }

    @Override
    public Exam save(Exam exam) {
        return jpaExamDAO.saveAndFlush(toData(exam)).toEntity();
    }

    @Override
    public void deleteAll() {
        jpaExamDAO.deleteAll();
    }

    @Override
    @Transactional
    public Answer saveAnswer(Answer answer) {
        // TODO: find out if there is an auto-incremental way to generate the answer's number
        int count = jpaAnswerDAO.countAllByExamIdAndProblemIdAndStudentId(answer.getExamId(), answer.getProblemId(), answer.getStudentId());
        answer.getId().setNumber(count + 1);
        AnswerData data = jpaAnswerDAO.saveAndFlush(AnswerData.toData(answer));
        return data.toEntity();
    }

    @Override
    public Optional<Answer> findAnswer(Answer.Id id) {
        return jpaAnswerDAO.findById(new AnswerData.Id(id))
                .map(AnswerData::toEntity);
    }

    @Override
    public int countAnswersInQuestion(Question.Id id, int studentId) {
        return jpaAnswerDAO.countAllByExamIdAndProblemIdAndStudentId(id.getExamId(), id.getProblemId(), studentId);
    }

    @Override
    public boolean isExaminee(int studentId, int examId) {
        return jpaExamineeDAO.existsById_StudentIdAndId_ExamId(studentId, examId);
    }

    @Override
    public void deleteExamById(int examId) {
        try {
            jpaExamDAO.deleteById(examId);
        } catch (EmptyResultDataAccessException ignored) {
        }
    }

}
