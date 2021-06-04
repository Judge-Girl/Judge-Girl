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
import tw.waterball.judgegirl.primitives.submission.events.LiveSubmissionEvent;
import tw.waterball.judgegirl.primitives.submission.events.VerdictIssuedEvent;
import tw.waterball.judgegirl.springboot.profiles.productions.Amqp;
import tw.waterball.judgegirl.submissionapi.clients.EventPublisher;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Amqp
@Component
public class AmqpEventPublisher implements EventPublisher {
    private final AmqpTemplate amqpTemplate;
    private final TopicExchange submissionsExchange;
    private final String verdictIssueRoutingKey;
    private final String liveSubmissionsRoutingKey;

    public AmqpEventPublisher(AmqpAdmin amqpAdmin, AmqpTemplate amqpTemplate,
                              @Value("${judge-girl.amqp.submissions-exchange-name}")
                                      String submissionsExchange,
                              @Value("${judge-girl.amqp.verdict-issued-routing-key-format}")
                                      String verdictIssuedRoutingKeyFormat,
                              @Value("${judge-girl.amqp.live-submissions-routing-key}")
                                      String liveSubmissionsRoutingKey) {
        this.amqpTemplate = amqpTemplate;
        this.submissionsExchange = new TopicExchange(submissionsExchange);
        this.verdictIssueRoutingKey = verdictIssuedRoutingKeyFormat;
        this.liveSubmissionsRoutingKey = liveSubmissionsRoutingKey;
        amqpAdmin.declareExchange(this.submissionsExchange);
    }

    @SneakyThrows
    @Override
    public void publish(VerdictIssuedEvent event) {
        String routingKey = String.format(verdictIssueRoutingKey, event.getSubmissionId());
        amqpTemplate.convertAndSend(submissionsExchange.getName(),
                routingKey, event);
    }

    @Override
    public void publish(LiveSubmissionEvent event) {
        amqpTemplate.convertAndSend(submissionsExchange.getName(),
                liveSubmissionsRoutingKey, event);
    }

}
