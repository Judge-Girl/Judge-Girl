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

import lombok.*;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.primitives.submission.Bag;

import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Builder
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SubmitCodeRequest {
    public boolean throttle = true;
    public int problemId;
    public String languageEnvName;
    public int studentId;
    public List<FileResource> fileResources;

    public Bag submissionBag;

    public SubmitCodeRequest(int problemId, String languageEnvName, int studentId, List<FileResource> fileResources) {
        this(problemId, languageEnvName, studentId, fileResources, Bag.empty());
    }

    public SubmitCodeRequest(int problemId, String languageEnvName, int studentId, List<FileResource> fileResources, Bag submissionBag) {
        this.problemId = problemId;
        this.languageEnvName = languageEnvName;
        this.studentId = studentId;
        this.fileResources = fileResources;
        this.submissionBag = submissionBag;
    }
}