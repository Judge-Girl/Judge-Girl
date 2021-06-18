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

package tw.waterball.judgegirl.submission.domain.usecases.query;


import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@Builder
public class SubmissionQueryParams {
    public static final SubmissionQueryParams EMPTY = SubmissionQueryParams.builder().build();
    @Nullable
    private final Integer page;
    @Nullable
    private final Integer problemId;
    @Nullable
    private final String languageEnvName;
    @Nullable
    private final Integer studentId;

    @Builder.Default
    private Map<String, String> bagQueryParameters = new HashMap<>();

    @Nullable
    private final SortBy sortBy;

    public SubmissionQueryParams(@Nullable Integer page, @Nullable Integer problemId,
                                 @Nullable String languageEnvName, @Nullable Integer studentId,
                                 Map<String, String> bagQueryParameters,
                                 @Nullable SortBy sortBy) {
        this.page = page;
        this.problemId = problemId;
        this.languageEnvName = languageEnvName;
        this.studentId = studentId;
        this.bagQueryParameters = bagQueryParameters;
        this.sortBy = sortBy;
    }

    public static SubmissionQueryParamsBuilder query() {
        return SubmissionQueryParams.builder();
    }

    public Optional<Integer> getPage() {
        return ofNullable(page);
    }

    public Optional<String> getLanguageEnvName() {
        return ofNullable(languageEnvName);
    }

    public Optional<Integer> getProblemId() {
        return ofNullable(problemId);
    }

    public Optional<Integer> getStudentId() {
        return ofNullable(studentId);
    }

    public Map<String, String> getBagQueryParameters() {
        return bagQueryParameters;
    }

    public Optional<SortBy> getSortBy() {
        return ofNullable(sortBy);
    }
}
