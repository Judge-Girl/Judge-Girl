package tw.waterball.judgegirl.springboot.academy.controllers;

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tw.waterball.judgegirl.problemapi.clients.FakeProblemServiceDriver;
import tw.waterball.judgegirl.studentapi.clients.FakeStudentServiceDriver;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;

import static org.mockito.Mockito.mock;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Configuration
public class TestConfig {
    @Bean
    @Primary
    public ConnectionFactory mockRabbitMqConnectionFactory() {
        return new CachingConnectionFactory(new MockConnectionFactory());
    }

    @Bean
    @Primary
    public FakeProblemServiceDriver fakeProblemServiceDriver() {
        return new FakeProblemServiceDriver();
    }


    @Bean
    @Primary
    public FakeStudentServiceDriver fakeStudentServiceDriver() {
        return new FakeStudentServiceDriver();
    }

    @Bean
    @Primary
    public SubmissionServiceDriver submissionServiceDriver() {
        return mock(SubmissionServiceDriver.class);
    }
}