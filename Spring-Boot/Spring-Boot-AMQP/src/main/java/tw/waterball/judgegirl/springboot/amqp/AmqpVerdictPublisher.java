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

package tw.waterball.judgegirl.springboot.amqp;

import lombok.SneakyThrows;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.primitives.submission.events.VerdictIssuedEvent;
import tw.waterball.judgegirl.springboot.profiles.productions.Amqp;
import tw.waterball.judgegirl.submissionapi.clients.VerdictPublisher;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Amqp
@Component
public class AmqpVerdictPublisher implements VerdictPublisher {
    private final AmqpTemplate amqpTemplate;
    private final TopicExchange verdictExchange;
    private final String verdictIssueRoutingKey;

    public AmqpVerdictPublisher(AmqpAdmin amqpAdmin, AmqpTemplate amqpTemplate,
                                @Value("${judge-girl.amqp.verdict-exchange-name}")
                                        String verdictExchangeName,
                                @Value("${judge-girl.amqp.verdict-issued-routing-key-format}")
                                        String verdictIssuedRoutingKeyFormat) {
        this.amqpTemplate = amqpTemplate;
        this.verdictExchange = new TopicExchange(verdictExchangeName);
        this.verdictIssueRoutingKey = verdictIssuedRoutingKeyFormat;
        amqpAdmin.declareExchange(verdictExchange);
    }

    @SneakyThrows
    @Override
    public void publish(VerdictIssuedEvent event) {
        String routingKey = getRoutingKey(event.getSubmissionId());
        amqpTemplate.convertAndSend(verdictExchange.getName(),
                routingKey, event);
    }

    private String getRoutingKey(String submissionId) {
        return String.format(verdictIssueRoutingKey, submissionId);
    }
}
