package tw.waterball.judgegirl.springboot.exam.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.entities.exam.ExamHasNotBeenStartedException;
import tw.waterball.judgegirl.entities.exam.NoSubmissionQuotaException;
import tw.waterball.judgegirl.entities.exam.Record;
import tw.waterball.judgegirl.entities.submission.SubmissionThrottlingException;
import tw.waterball.judgegirl.entities.submission.verdict.VerdictIssuedEvent;
import tw.waterball.judgegirl.examservice.domain.usecases.AnswerQuestionUseCase;

import static tw.waterball.judgegirl.springboot.exam.view.AnswerView.toViewModel;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Aspect
@Component
@AllArgsConstructor
public class LoggingAspect {
    private final ObjectMapper objectMapper;

    @Around("execution(* tw.waterball.judgegirl.examservice.domain.usecases.AnswerQuestionUseCase.execute(" +
            "tw.waterball.judgegirl.examservice.domain.usecases.AnswerQuestionUseCase.Request, " +
            "tw.waterball.judgegirl.examservice.domain.usecases.AnswerQuestionUseCase.Presenter))")
    public Object logAnswerQuestionUseCase(ProceedingJoinPoint joinPoint) throws Throwable {
        var args = joinPoint.getArgs();
        var request = (AnswerQuestionUseCase.Request) args[0];
        var presenter = (AnswerQuestionUseCase.Presenter) args[1];
        var useCase = (AnswerQuestionUseCase) joinPoint.getTarget();
        Logger log = LoggerFactory.getLogger(useCase.getClass());
        String requestToJson = String.format("{\"examId\":%d, \"problemId\":%d, " +
                        "\"langEnvName\":\"%s\", \"studentId\":%d, \"fileCount\":%d}",
                request.getExamId(), request.getProblemId(), request.getLangEnvName(),
                request.getStudentId(), request.getFileResources().size());
        log.info("[Answer Question] {}", requestToJson);
        args[1] = (AnswerQuestionUseCase.Presenter) answer -> {
            log.info("[Answer Question Successfully] {}", toJson(toViewModel(answer)));
            presenter.showAnswer(answer);
        };

        try {
            return joinPoint.proceed(args);
        } catch (SubmissionThrottlingException err) {
            log.error("[Answer Question Failed: Submission Throttled] {}", requestToJson);
            throw err;
        } catch (ExamHasNotBeenStartedException err) {
            log.error("[Answer Question Failed: Exam Has Not Been Started] {}", requestToJson);
            throw err;
        } catch (NoSubmissionQuotaException err) {
            log.error("[Answer Question Failed: No Submission Quota] {}", requestToJson);
            throw err;
        }
    }

    @Around("execution(* tw.waterball.judgegirl.examservice.domain.usecases.AnswerQuestionUseCase.updateBestRecordFromVerdict(..))")
    public Object logOnVerdictIssued(ProceedingJoinPoint joinPoint) throws Throwable {
        var args = joinPoint.getArgs();
        var event = (VerdictIssuedEvent) args[0];
        var useCase = (AnswerQuestionUseCase) joinPoint.getTarget();
        Logger log = LoggerFactory.getLogger(useCase.getClass());
        log.info("[Saving Best Record From Verdict Issued Event] {}", toJson(event));
        Record bestRecord = (Record) joinPoint.proceed(args);
        log.info("[Saved Best Record] {}", toJson(bestRecord));
        return bestRecord;
    }


    @SneakyThrows
    private String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }
}
