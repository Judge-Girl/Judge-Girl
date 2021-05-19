package tw.waterball.judgegirl.springboot.problem.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;
import tw.waterball.judgegirl.springboot.problem.repositories.CacheProblemRepository;
import tw.waterball.judgegirl.springboot.profiles.productions.Redis;

/**
 * @author - wally55077@gmail.com
 */
@Redis
@Configuration
public class RedisConfiguration {

    @Bean
    public ProblemRepository problemRepository(ObjectMapper objectMapper,
                                               RedisTemplate<String, String> redisTemplate,
                                               ProblemRepository problemRepository) {
        return new CacheProblemRepository(objectMapper, redisTemplate, problemRepository);
    }

}
