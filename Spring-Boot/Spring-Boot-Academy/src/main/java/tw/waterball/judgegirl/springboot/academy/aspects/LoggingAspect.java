package tw.waterball.judgegirl.springboot.academy.aspects;

import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.academy.domain.usecases.exam.AnswerQuestionUseCase;
import tw.waterball.judgegirl.primitives.exam.ExamHasNotBeenStartedOrHasBeenClosedException;
import tw.waterball.judgegirl.primitives.exam.NoSubmissionQuotaException;
import tw.waterball.judgegirl.primitives.submission.SubmissionThrottlingException;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Aspect
@Component
@AllArgsConstructor
public class LoggingAspect {
    @Around("execution(* tw.waterball.judgegirl.academy.domain.usecases.exam.AnswerQuestionUseCase.execute(" +
            "tw.waterball.judgegirl.academy.domain.usecases.exam.AnswerQuestionUseCase.Request, " +
            "tw.waterball.judgegirl.academy.domain.usecases.exam.AnswerQuestionUseCase.Presenter))")
    public Object logAnswerQuestionUseCase(ProceedingJoinPoint joinPoint) throws Throwable {
        var args = joinPoint.getArgs();
        var request = (AnswerQuestionUseCase.Request) args[0];
        var presenter = (AnswerQuestionUseCase.Presenter) args[1];
        var useCase = (AnswerQuestionUseCase) joinPoint.getTarget();
        Logger log = LoggerFactory.getLogger(useCase.getClass());
        log.info("[Answer the Question] examId={} problemId={} langEnvName={} studentId={} fileCount={}",
                request.getExamId(), request.getProblemId(), request.getLangEnvName(),
                request.getStudentId(), request.getFileResources().size());

        try {
            return joinPoint.proceed(args);
        } catch (SubmissionThrottlingException err) {
            log.info("[Answer Question Failed] Submission Throttled\"");
            throw err;
        } catch (ExamHasNotBeenStartedOrHasBeenClosedException err) {
            log.warn("[Answer Question Failed] Exam Has Not Been Started ({})\"", err.getDuration());
            throw err;
        } catch (NoSubmissionQuotaException err) {
            log.info("[Answer Question Failed] No remaining submission Quota out of {}\"", err.getSubmissionQuota());
            throw err;
        }
    }

}
