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

package tw.waterball.judgegirl.springboot.submission.configs;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.apis.BatchV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.waterball.judgegirl.commons.utils.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@ConditionalOnProperty(name = "judge-girl.judger.strategy",
        havingValue = K8SDeployerAutoConfiguration.STRATEGY)
@Configuration
public class K8SDeployerAutoConfiguration {
    public static final String STRATEGY = "kubernetes";

    @Bean
    public ApiClient apiClient() throws IOException {
        InputStream in = ResourceUtils.getResourceAsStream("/config/kube/kubeconfig");
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(
                        new InputStreamReader(in, StandardCharsets.UTF_8))).build();
        // set the global default api-client to the in-cluster one from above
        io.kubernetes.client.Configuration.setDefaultApiClient(client);

        return client;
    }

    @Bean
    public BatchV1Api coreV1Api(ApiClient apiClient) {
        return new BatchV1Api(apiClient);
    }
}
