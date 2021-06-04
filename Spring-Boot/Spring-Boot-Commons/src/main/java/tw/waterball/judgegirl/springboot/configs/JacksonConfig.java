/*
 * Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package tw.waterball.judgegirl.springboot.configs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import tw.waterball.judgegirl.springboot.configs.jackson.LiveSubmissionEventJacksonConfig;
import tw.waterball.judgegirl.springboot.configs.jackson.VerdictIssuedEventJacksonConfig;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Configuration
public class JacksonConfig {
    public static final ObjectMapper OBJECT_MAPPER;

    static {
        var objectMapperBuilder = new Jackson2ObjectMapperBuilder();
        new JacksonConfig()
                .jsonCustomizer(new JsonDeserializer<?>[]{
                                VerdictIssuedEventJacksonConfig.DESERIALIZER,
                                LiveSubmissionEventJacksonConfig.DESERIALIZER},
                        new JsonSerializer<?>[]{
                                VerdictIssuedEventJacksonConfig.SERIALIZER,
                                LiveSubmissionEventJacksonConfig.SERIALIZER})
                .customize(objectMapperBuilder);
        OBJECT_MAPPER = objectMapperBuilder.build();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer(JsonDeserializer<?>[] jsonDeserializers,
                                                                JsonSerializer<?>[] jsonSerializers) {
        return builder -> builder.serializationInclusion(JsonInclude.Include.NON_NULL)
                .failOnUnknownProperties(false)
                .featuresToEnable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .deserializers(jsonDeserializers)
                .serializers(jsonSerializers);
    }
}

