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

package tw.waterball.judgegirl.springboot.submission.amqp;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.utils.NotifyWaitLock;
import tw.waterball.judgegirl.primitives.submission.events.VerdictIssuedEvent;
import tw.waterball.judgegirl.submission.domain.repositories.SubmissionRepository;
import tw.waterball.judgegirl.submission.domain.usecases.VerdictIssuedEventHandler;

import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Slf4j
@Component
@AllArgsConstructor
public class VerdictIssuedEventListener {
    private final List<VerdictIssuedEventHandler> handlers;

    private final SubmissionRepository submissionRepository;


    // This is for test-purpose, test script can wait for this lock until the next onIssueVerdict()
    public final NotifyWaitLock onHandlingCompletion$ = new NotifyWaitLock();

    @RabbitListener(queues = "${judge-girl.amqp.submission-service-queue}")
    public void listen(VerdictIssuedEvent event) {
        log.trace("[Consume: {}] {}", event.getName(), event);
        handlers.forEach(handler -> handler.handle(event));
        onHandlingCompletion$.doNotifyAll();
    }

}
