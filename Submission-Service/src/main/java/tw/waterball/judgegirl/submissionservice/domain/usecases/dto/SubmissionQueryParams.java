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

package tw.waterball.judgegirl.submissionservice.domain.usecases.dto;


import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class SubmissionQueryParams {
    @Nullable
    private Integer page;
    private int problemId;
    private String languageEnvName;
    private int studentId;

    public SubmissionQueryParams(@Nullable Integer page, int problemId, String languageEnvName, int studentId) {
        this.page = page;
        this.problemId = problemId;
        this.languageEnvName = languageEnvName;
        this.studentId = studentId;
    }

    public Optional<Integer> getPage() {
        return Optional.ofNullable(page);
    }

    public String getLanguageEnvName() {
        return languageEnvName;
    }

    public int getProblemId() {
        return problemId;
    }

    public int getStudentId() {
        return studentId;
    }
}
