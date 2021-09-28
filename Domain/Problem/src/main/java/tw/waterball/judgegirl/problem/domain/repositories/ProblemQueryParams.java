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

package tw.waterball.judgegirl.problem.domain.repositories;


import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ProblemQueryParams {
    public static final ProblemQueryParams NO_PARAMS = new ProblemQueryParams(null, null, false, true, true);

    private final String[] tags;

    @Nullable
    private final Integer page;

    @Nullable
    private final Boolean archivedFlag;

    private final boolean includeVisibleProblems;

    private final boolean includeInvisibleProblems;

    public ProblemQueryParams(String[] tags, @Nullable Integer page, @Nullable Boolean archivedFlag, boolean includeVisibleProblems, boolean includeInvisibleProblems) {
        this.tags = tags;
        this.page = page;
        this.archivedFlag = archivedFlag;
        this.includeVisibleProblems = includeVisibleProblems;
        this.includeInvisibleProblems = includeInvisibleProblems;
    }

    public String[] getTags() {
        return tags;
    }

    public Optional<Integer> getPage() {
        return ofNullable(page);
    }

    public Optional<Boolean> archivedFlag() {
        return ofNullable(archivedFlag);
    }

    public boolean includeVisibleProblems() {
        return includeVisibleProblems;
    }

    public boolean includeInvisibleProblems() {
        return includeInvisibleProblems;
    }
}
