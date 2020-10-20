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

import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.springboot.profiles.Dev;
import tw.waterball.judgegirl.submissionservice.ports.SubmissionMessageQueue;
import tw.waterball.judgegirl.submissionservice.ports.VerdictIssuedEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Dev
@Component
public class StubSubmissionMessageQueue implements SubmissionMessageQueue {
    private boolean running = false;
    private Set<VerdictIssueListener> verdictIssueListeners = new HashSet<>();

    @Override
    public void startListening() {
        running = true;
    }

    @Override
    public void stopListening() {
        running = false;
    }

    @Override
    public synchronized void publish(VerdictIssuedEvent event) {
        if (running) {
            verdictIssueListeners.forEach(l -> l.onVerdictIssued(event));
        }
    }

    @Override
    public synchronized void subscribe(VerdictIssueListener verdictIssueListener) {
        verdictIssueListeners.add(verdictIssueListener);
    }

    @Override
    public void unsubscribe(VerdictIssueListener verdictIssueListener) {
        verdictIssueListeners.remove(verdictIssueListener);
    }

}
