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

package tw.waterball.judgegirl.springboot.submission.impl.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.entities.submission.JudgeResponse;
import tw.waterball.judgegirl.submissionservice.ports.SubmissionMessageQueue;
import tw.waterball.judgegirl.springboot.profiles.productions.Amqp;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Amqp
@Component
public class AmqpSubmissionMessageQueue implements SubmissionMessageQueue {
    private boolean running = false;
    private AmqpAdmin amqpAdmin;
    private Queue judgeResponseQueue;
    private AmqpTemplate amqpTemplate;
    private TopicExchange submissionTopicExchange;
    private ObjectMapper objectMapper;
    private Set<Listener> listeners = new HashSet<>();

    @Autowired
    public AmqpSubmissionMessageQueue(AmqpAdmin amqpAdmin, AmqpTemplate amqpTemplate,
                                      ObjectMapper objectMapper) {
        this.amqpAdmin = amqpAdmin;
        this.amqpTemplate = amqpTemplate;
        this.objectMapper = objectMapper;
        this.judgeResponseQueue = new Queue("Submission-Service:Judge-Response", true);
        this.submissionTopicExchange = new TopicExchange("submissions");
        amqpAdmin.declareQueue(judgeResponseQueue);
        amqpAdmin.declareExchange(submissionTopicExchange);
    }

    @Override
    public void publish(JudgeResponse judgeResponse) {
        String submissionId = judgeResponse.getSubmissionId();
        String routingKey = getRoutingKey(submissionId);
        try {
            amqpTemplate.convertAndSend(submissionTopicExchange.getName(), routingKey, objectMapper.writeValueAsBytes(judgeResponse));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void subscribe(Listener listener) {
        listeners.add(listener);
        amqpAdmin.declareBinding(BindingBuilder.bind(judgeResponseQueue)
                .to(submissionTopicExchange)
                .with(getRoutingKey("*"))); // listen to all submissions
    }

    @Override
    public void unsubscribe(Listener listener) {
        listeners.remove(listener);
    }


    private String getRoutingKey(String submissionId) {
        return String.format("submissions.%s.judge", submissionId);
    }

    @Override
    public void startListening() {
        running = true;
        new Thread(this::continuouslyListening).start();
    }

    @Override
    public void stopListening() {
        running = false;
    }

    private void continuouslyListening() {
        while (running) {
            try {
                Message message = amqpTemplate.receive(judgeResponseQueue.getName(), -1);
                if (message != null) {
                    synchronized (this) {
                        String json = new String(message.getBody(), StandardCharsets.UTF_8);
                        JudgeResponse judgeResponse = objectMapper.readValue(json, JudgeResponse.class);
                        listeners.forEach(l -> l.onBroadcast(judgeResponse));
                    }
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }

}
