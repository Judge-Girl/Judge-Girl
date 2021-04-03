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

package tw.waterball.judgegirl.springboot.submission.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.utils.NotifyWaitLock;
import tw.waterball.judgegirl.entities.submission.Verdict;
import tw.waterball.judgegirl.submissionapi.views.VerdictIssuedEvent;
import tw.waterball.judgegirl.submissionapi.views.VerdictView;
import tw.waterball.judgegirl.submissionservice.domain.repositories.SubmissionRepository;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Slf4j
@Component
@AllArgsConstructor
public class VerdictIssuedEventHandler {
    private final SubmissionRepository submissionRepository;


    // This is for test-purpose, test script can wait for this lock until the next onIssueVerdict()
    public final NotifyWaitLock onHandlingCompletion$ = new NotifyWaitLock();

    @RabbitListener(queues = "${judge-girl.amqp.submission-service-queue}")
    public void listen(VerdictIssuedEvent event) {
        log.info("Handle: {}", event);

        Verdict verdict = VerdictView.toEntity(event.getVerdict());
        submissionRepository.issueVerdictOfSubmission(event.getSubmissionId(), verdict);

        onHandlingCompletion$.doNotifyAll();
    }
}
