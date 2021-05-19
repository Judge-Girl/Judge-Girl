package tw.waterball.judgegirl.springboot.configs;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import tw.waterball.judgegirl.springboot.profiles.productions.Redis;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author - wally55077@gmail.com
 */
@Redis
@Setter
@Configuration
@ConfigurationProperties("judge-girl.redis")
public class RedisConfig {

    private String host;

    private int port;

    private String password;

    private String keyPrefix;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        var redisConfig = new RedisStandaloneConfiguration(host, port);
        redisConfig.setPassword(password);
        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate(redisConnectionFactory);
        redisTemplate.setKeySerializer(new JudgeGirlRedisSerializer(keyPrefix));
        return redisTemplate;
    }

    @AllArgsConstructor
    private static final class JudgeGirlRedisSerializer implements RedisSerializer<String> {

        private final String keyPrefix;

        @Override
        public byte[] serialize(String s) throws SerializationException {
            return (keyPrefix + s).getBytes(UTF_8);
        }

        @Override
        public String deserialize(byte[] bytes) throws SerializationException {
            return new String(bytes, keyPrefix.length(), bytes.length - keyPrefix.length(), UTF_8);
        }
    }

}
