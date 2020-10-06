/*
 *  Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tw.waterball.judgegirl.springboot.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Configuration
public class ApplicationContextUtils {

    public static ApplicationContext setupSpringApplicationContext(String[] profiles, Class... otherComponentClasses) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        // TODO refactoring...
        // context.register(ScanRoot.class, ApplicationContextUtils.class);
        for (Class componentClass : otherComponentClasses) {
            context.register(componentClass);
        }
        context.getEnvironment().setActiveProfiles(profiles);

        context.refresh();
        return context;
    }

    @Bean
    public PropertySourcesPlaceholderConfigurer myPropertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer pph = new PropertySourcesPlaceholderConfigurer();
        pph.setLocations(
                new ClassPathResource("/application-dev.properties"),
                new ClassPathResource("/application-mongo.properties"),
                new ClassPathResource("/application-amqp.properties"),
                new ClassPathResource("/application-prod.properties"),
                new ClassPathResource("/application-jwt.properties"),
                new ClassPathResource("/application-serviceDriver.properties"),
                new ClassPathResource("/judge-girl.properties"));
        return pph;
    }
}
