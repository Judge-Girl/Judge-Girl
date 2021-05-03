package tw.waterball.judgegirl.springboot.academy.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.academy.domain.usecases.exam.AnswerQuestionUseCase;
import tw.waterball.judgegirl.primitives.exam.ExamHasNotBeenStartedException;
import tw.waterball.judgegirl.primitives.exam.NoSubmissionQuotaException;
import tw.waterball.judgegirl.primitives.submission.SubmissionThrottlingException;

import static tw.waterball.judgegirl.springboot.academy.view.AnswerView.toViewModel;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Aspect
@Component
@AllArgsConstructor
public class LoggingAspect {
    private final ObjectMapper objectMapper;

    @Around("execution(* tw.waterball.judgegirl.academy.domain.usecases.exam.AnswerQuestionUseCase.execute(" +
            "tw.waterball.judgegirl.academy.domain.usecases.exam.AnswerQuestionUseCase.Request, " +
            "tw.waterball.judgegirl.academy.domain.usecases.exam.AnswerQuestionUseCase.Presenter))")
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


    @SneakyThrows
    private String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }
}
