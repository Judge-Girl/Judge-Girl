package tw.waterball.judgegirl.springboot.exam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.waterball.judgegirl.examservice.domain.usecases.exam.AnswerQuestionUseCase;
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
                    bag.put("broker-stomp-additional-destination-split-by-commas", "exams/" + examId);
                });
    }
}
