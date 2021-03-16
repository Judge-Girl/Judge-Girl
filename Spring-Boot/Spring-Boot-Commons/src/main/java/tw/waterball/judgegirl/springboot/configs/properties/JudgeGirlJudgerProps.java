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

package tw.waterball.judgegirl.springboot.configs.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Getter
@Setter
@ConfigurationProperties("judge-girl.judger")
public class JudgeGirlJudgerProps {
    private String strategy;
    private String jwtToken;
    @NestedConfigurationProperty
    private Job job;
    @NestedConfigurationProperty
    private Image image;
    @NestedConfigurationProperty
    private Container container;
    @NestedConfigurationProperty
    private Docker docker;
    @NestedConfigurationProperty
    private Kubernetes kubernetes;


    @Getter
    @Setter
    public static class Job {
        private String nameFormat;
    }

    @Getter
    @Setter
    public static class Image {
        private String name;
    }

    @Getter
    @Setter
    public static class Container {
        private String nameFormat;
    }

    @Getter
    @Setter
    public static class Docker {
        private String network;
    }

    @Getter
    @Setter
    public static class Kubernetes {
        private String imagePullSecret;
    }
}
