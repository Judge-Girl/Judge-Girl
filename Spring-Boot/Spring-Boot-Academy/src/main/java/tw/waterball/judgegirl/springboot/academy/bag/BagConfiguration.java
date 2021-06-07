package tw.waterball.judgegirl.springboot.academy.bag;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.waterball.judgegirl.academy.domain.usecases.exam.AnswerQuestionUseCase;
import tw.waterball.judgegirl.submissionapi.clients.BagInterceptor;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Configuration
public class BagConfiguration {
    @Bean
    public BagInterceptor bagInterceptor() {
        return bag -> bag.getAsInteger(AnswerQuestionUseCase.BAG_KEY_EXAM_ID)
                .ifPresent(examId -> {
                    // Add the exam's broker destination, see "tw.waterball.judgegirl.springboot.student.broker"
                    // TODO: [must improve] weak reference using String instead of constant, consider improving this by adding a constant in API/Student-API
                    bag.put("broker-additional-destinations", "/exams/" + examId);
                });
    }
}
