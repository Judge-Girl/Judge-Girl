package tw.waterball.judgegirl.springboot.academy.aspects;

import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.academy.domain.repositories.ExamFilter;
import tw.waterball.judgegirl.academy.domain.usecases.exam.*;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.primitives.exam.ExamHasNotBeenStartedOrHasBeenClosedException;
import tw.waterball.judgegirl.primitives.exam.NoSubmissionQuotaException;
import tw.waterball.judgegirl.primitives.submission.SubmissionThrottlingException;

import static tw.waterball.judgegirl.commons.utils.DateUtils.format;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.sum;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Aspect
@Component
@AllArgsConstructor
public class LoggingAspect {
    private static final Logger ANSWER_QUESTION_USE_CASE_LOGGER = LoggerFactory.getLogger(AnswerQuestionUseCase.class);
    private static final Logger CREATE_EXAM_USE_CASE_LOGGER = LoggerFactory.getLogger(CreateExamUseCase.class);
    private static final Logger GET_EXAMS_USE_CASE_LOGGER = LoggerFactory.getLogger(GetExamsUseCase.class);
    private static final Logger GET_EXAM_PROGRESS_OVERVIEW_USE_CASE_LOGGER = LoggerFactory.getLogger(GetExamProgressOverviewUseCase.class);
    private static final Logger GET_EXAM_OVERVIEW_USE_CASE_LOGGER = LoggerFactory.getLogger(GetExamOverviewUseCase.class);
    private static final Logger UPDATE_EXAM_USE_CASE_LOGGER = LoggerFactory.getLogger(UpdateExamUseCase.class);
    private static final Logger GET_EXAM_USE_CASE_LOGGER = LoggerFactory.getLogger(GetExamUseCase.class);
    private static final Logger DELETE_EXAMINEES_USE_CASE_LOGGER = LoggerFactory.getLogger(DeleteExamineesUseCase.class);
    private static final Logger DELETE_EXAM_USE_CASE_LOGGER = LoggerFactory.getLogger(DeleteExamUseCase.class);
    private static final Logger ADD_GROUP_OF_EXAMINEES_USE_CASE_LOGGER = LoggerFactory.getLogger(AddGroupOfExamineesUseCase.class);
    private static final Logger GET_EXAMINEES_USE_CASE_LOGGER = LoggerFactory.getLogger(GetExamineesUseCase.class);
    private static final Logger ADD_EXAMINEES_USE_CASE_LOGGER = LoggerFactory.getLogger(AddExamineesUseCase.class);
    private static final Logger CALCULATE_EXAM_SCORE_USE_CASE_LOGGER = LoggerFactory.getLogger(CalculateExamScoreUseCase.class);
    private static final Logger CREATE_QUESTION_USE_CASE_LOGGER = LoggerFactory.getLogger(CreateQuestionUseCase.class);
    private static final Logger UPDATE_QUESTION_USE_CASE_LOGGER = LoggerFactory.getLogger(UpdateQuestionUseCase.class);
    private static final Logger DELETE_QUESTION_USE_CASE_LOGGER = LoggerFactory.getLogger(DeleteQuestionUseCase.class);

    @Around("execution(* tw.waterball.judgegirl.academy.domain.usecases.exam.AnswerQuestionUseCase.execute(" +
            "tw.waterball.judgegirl.academy.domain.usecases.exam.AnswerQuestionUseCase.Request, " +
            "tw.waterball.judgegirl.academy.domain.usecases.exam.AnswerQuestionUseCase.Presenter))")
    public Object logAnswerQuestionUseCase(ProceedingJoinPoint joinPoint) throws Throwable {
        var args = joinPoint.getArgs();
        var request = (AnswerQuestionUseCase.Request) args[0];
        var fileResources = request.getFileResources();
        ANSWER_QUESTION_USE_CASE_LOGGER.info("[Answer the Question] examId={} problemId={} langEnvName=\"{}\" studentId={} fileContentLengthSum={} fileCount={}",
                request.getExamId(), request.getProblemId(), request.getLangEnvName(),
                request.getStudentId(), sum(fileResources, FileResource::getContentLength), fileResources.size());

        try {
            return joinPoint.proceed(args);
        } catch (SubmissionThrottlingException err) {
            ANSWER_QUESTION_USE_CASE_LOGGER.info("[Answer Question Failed] Submission Throttled\"");
            throw err;
        } catch (ExamHasNotBeenStartedOrHasBeenClosedException err) {
            ANSWER_QUESTION_USE_CASE_LOGGER.warn("[Answer Question Failed] Exam Has Not Been Started ({})\"", err.getDuration());
            throw err;
        } catch (NoSubmissionQuotaException err) {
            ANSWER_QUESTION_USE_CASE_LOGGER.info("[Answer Question Failed] No remaining submission Quota out of {}\"", err.getSubmissionQuota());
            throw err;
        }
    }

    @Before("bean(createExamUseCase)")
    public void logCreateExamUseCase(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        var request = (CreateExamUseCase.Request) args[0];
        CREATE_EXAM_USE_CASE_LOGGER.info("[Create Exam] name=\"{}\" startTime=\"{}\" endTime=\"{}\" description=\"{}\"",
                request.getName(), format(request.getStartTime()), format(request.getEndTime()), request.getDescription());
    }

    @Before("bean(getExamsUseCase)")
    public void logGetExamsUseCase(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        var examFilter = (ExamFilter) args[0];
        GET_EXAMS_USE_CASE_LOGGER.info("[Get Exams] studentId={} status=\"{}\" skip={} size={}",
                examFilter.getStudentId(), examFilter.getStatus(), examFilter.getSkip(), examFilter.getSize());
    }

    @Before("bean(getExamProgressOverviewUseCase)")
    public void logGetExamProgressOverviewUseCase(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        var request = (GetExamProgressOverviewUseCase.Request) args[0];
        GET_EXAM_PROGRESS_OVERVIEW_USE_CASE_LOGGER.info("[Get ExamProgress Overview] examId={} studentId={}",
                request.getStudentId(), request.getStudentId());
    }

    @Before("bean(getExamOverviewUseCase)")
    public void logGetExamOverviewUseCase(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        var examId = (Integer) args[0];
        GET_EXAM_OVERVIEW_USE_CASE_LOGGER.info("[Get Exam Overview] examId={}", examId);
    }

    @Before("bean(updateExamUseCase)")
    public void logUpdateExamUseCase(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        var request = (UpdateExamUseCase.Request) args[0];
        UPDATE_EXAM_USE_CASE_LOGGER.info("[Update Exam] examId={} name=\"{}\" startTime=\"{}\" endTime=\"{}\" description=\"{}\"",
                request.getExamId(), request.getName(), format(request.getStartTime()), format(request.getEndTime()), request.getDescription());
    }

    @Before("bean(getExamUseCase)")
    public void logGetExamUseCase(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        var request = (GetExamUseCase.Request) args[0];
        GET_EXAM_USE_CASE_LOGGER.info("[Get Exam] examId={} onlyExamineeCanAccess={} studentId={}",
                request.getExamId(), request.isOnlyExamineeCanAccess(), request.getStudentId());
    }

    @Before("bean(deleteExamineesUseCase)")
    public void logDeleteExamineesUseCase(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        var request = (DeleteExamineesUseCase.Request) args[0];
        DELETE_EXAMINEES_USE_CASE_LOGGER.info("[Delete Examinees] examId={} emails={}",
                request.getExamId(), request.getEmails());
    }

    @Before("bean(deleteExamUseCase)")
    public void logDeleteExamUseCase(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        var examId = (Integer) args[0];
        DELETE_EXAM_USE_CASE_LOGGER.info("[Delete Exam] examId={}", examId);
    }

    @Before("bean(addGroupOfExamineesUseCase)")
    public void logAddGroupOfExamineesUseCase(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        var request = (AddGroupOfExamineesUseCase.Request) args[0];
        ADD_GROUP_OF_EXAMINEES_USE_CASE_LOGGER.info("[Add Group Of Examinees] examId={} names={}",
                request.getExamId(), request.getNames());
    }

    @Before("bean(getExamineesUseCase)")
    public void logGetExamineesUseCase(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        var request = (GetExamineesUseCase.Request) args[0];
        GET_EXAMINEES_USE_CASE_LOGGER.info("[Get Examinees] examId={} onlyExamineeCanAccess={} studentId={}",
                request.getExamId(), request.isOnlyExamineeCanAccess(), request.getStudentId());
    }

    @Before("bean(addExamineesUseCase)")
    public void logAddExamineesUseCase(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        var request = (AddExamineesUseCase.Request) args[0];
        ADD_EXAMINEES_USE_CASE_LOGGER.info("[Add Examinees] examId={} emails={}",
                request.getExamId(), request.getEmails());
    }

    @Before("bean(calculateExamScoreUseCase)")
    public void logCalculateExamScoreUseCase(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        var examId = (Integer) args[0];
        CALCULATE_EXAM_SCORE_USE_CASE_LOGGER.info("[Calculate Exam Score] examId={}", examId);
    }

    @Before("bean(createQuestionUseCase)")
    public void logCreateQuestionUseCase(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        var request = (CreateQuestionUseCase.Request) args[0];
        CREATE_QUESTION_USE_CASE_LOGGER.info("[Create Question] examId={} problemId={} quota={} score={} questionOrder={}",
                request.getExamId(), request.getProblemId(), request.getQuota(), request.getScore(), request.getQuestionOrder());
    }

    @Before("bean(updateQuestionUseCase)")
    public void logUpdateQuestionUseCase(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        var request = (UpdateQuestionUseCase.Request) args[0];
        UPDATE_QUESTION_USE_CASE_LOGGER.info("[Update Question] examId={} problemId={} quota={} score={} questionOrder={}",
                request.getExamId(), request.getProblemId(), request.getQuota(), request.getScore(), request.getQuestionOrder());
    }

    @Before("bean(deleteQuestionUseCase)")
    public void logDeleteQuestionUseCase(JoinPoint joinPoint) {
        var args = joinPoint.getArgs();
        var request = (DeleteQuestionUseCase.Request) args[0];
        DELETE_QUESTION_USE_CASE_LOGGER.info("[Delete Question] examId={} problemId={}",
                request.getExamId(), request.getProblemId());
    }

}
