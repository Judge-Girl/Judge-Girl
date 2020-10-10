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

package tw.waterball.judgegirl.springboot.submission.impl.k8s;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.BatchV1Api;
import io.kubernetes.client.models.V1Job;
import io.kubernetes.client.models.V1JobList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.waterball.judgegirl.commons.utils.ResourceUtils;
import tw.waterball.judgegirl.springboot.profiles.productions.K8S;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@K8S
@Configuration
public class K8sConfig {

    public static void main(String[] args) throws IOException, ApiException {
        K8sConfig k8sConfig = new K8sConfig();

        ApiClient apiClient = k8sConfig.apiClient();

        BatchV1Api batchV1Api = new BatchV1Api();
        V1JobList jobList = batchV1Api.listNamespacedJob("default", null, null, null, null, null, null, null, null, null);
        for (V1Job job : jobList.getItems()) {
            System.out.println(job.getMetadata().getName());
        }
//
//        CoreV1Api coreV1Api = new CoreV1Api();
//
//        System.out.println("Listing all namespaces: ");
//        V1NamespaceList namespaces = coreV1Api.listNamespace(null, null, null, null, null, null, null, null, null);
//        for (V1Namespace namespace : namespaces.getItems()) {
//            System.out.println(namespace.getMetadata().getName());
//        }
//
//        V1PodList list = coreV1Api.listNamespacedPod("default",
//                null, null, null,
//                null, null, null, null, null, null);
//
//        System.out.println("Listing all pods: ");
//        for (V1Pod item : list.getItems()) {
//            System.out.println(item.getMetadata().getName());
//        }
//
//        V1NodeList nodes = coreV1Api.listNode(null, null, null, null, null, null, null, null, null);
//        System.out.println("Listing all nodes: ");
//        for (V1Node node : nodes.getItems()) {
//            System.out.println(node.getMetadata().getName());
//        }
    }

    @Bean
    public ApiClient apiClient() throws IOException {
        InputStream in = ResourceUtils.getResourceAsStream("/kubeconfig");
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(
                        new InputStreamReader(in))).build();
        // set the global default api-client to the in-cluster one from above
        io.kubernetes.client.Configuration.setDefaultApiClient(client);

        return client;
    }

    @Bean
    public BatchV1Api coreV1Api(ApiClient apiClient) {
        return new BatchV1Api(apiClient);
    }
}
