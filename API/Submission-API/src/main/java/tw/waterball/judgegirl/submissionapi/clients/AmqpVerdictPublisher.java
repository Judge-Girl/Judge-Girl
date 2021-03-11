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

package tw.waterball.judgegirl.submissionapi.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.TopicExchange;
import tw.waterball.judgegirl.submissionapi.views.VerdictIssuedEvent;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class AmqpVerdictPublisher implements VerdictPublisher {
    private AmqpTemplate amqpTemplate;
    private TopicExchange submissionTopicExchange;
    private String verdictIssueRoutingKey;
    private ObjectMapper objectMapper;

    public AmqpVerdictPublisher(AmqpAdmin amqpAdmin, AmqpTemplate amqpTemplate,
                                ObjectMapper objectMapper,
                                String submissionExchangeName,
                                String verdictIssuedRoutingKeyFormat) {
        this.amqpTemplate = amqpTemplate;
        this.objectMapper = objectMapper;
        this.submissionTopicExchange = new TopicExchange(submissionExchangeName);
        this.verdictIssueRoutingKey = verdictIssuedRoutingKeyFormat;
        amqpAdmin.declareExchange(submissionTopicExchange);
    }

    @SneakyThrows
    @Override
    public void publish(VerdictIssuedEvent event) {
        String routingKey = getRoutingKey(event.getSubmissionId());
        amqpTemplate.convertAndSend(submissionTopicExchange.getName(),
                routingKey, objectMapper.writeValueAsBytes(event));
    }

    private String getRoutingKey(String submissionId) {
        return String.format(verdictIssueRoutingKey, submissionId);
    }
}
