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

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ProblemQueryParams {
    public static final ProblemQueryParams NO_PARAMS = new ProblemQueryParams(null, null, false, false, false);

    private final String[] tags;

    @Nullable
    private final Integer page;

    private final boolean excludeArchive;

    private final boolean includeArchive;

    private final boolean includeInvisibleProblems;

    private final boolean excludeVisibleProblems;

    public ProblemQueryParams(String[] tags, @Nullable Integer page, boolean includeInvisibleProblems, boolean excludeVisibleProblems, boolean includeArchive) {
        this(tags, page, true, includeInvisibleProblems, excludeVisibleProblems, includeArchive);
    }

    public ProblemQueryParams(String[] tags, @Nullable Integer page, boolean excludeArchive, boolean includeInvisibleProblems, boolean excludeVisibleProblems, boolean includeArchive) {
        this.tags = tags;
        this.page = page;
        this.excludeArchive = excludeArchive;
        this.includeArchive = includeArchive;
        this.includeInvisibleProblems = includeInvisibleProblems;
        this.excludeVisibleProblems = excludeVisibleProblems;
    }

    public String[] getTags() {
        return tags;
    }

    public Optional<Integer> getPage() {
        return Optional.ofNullable(page);
    }

    public boolean isExcludeArchive() {
        return excludeArchive;
    }

    public boolean isIncludeInvisibleProblems() {
        return includeInvisibleProblems;
    }

    public boolean isExcludeVisibleProblems() {
        return excludeVisibleProblems;
    }

    public boolean isIncludeArchive() {
        return includeArchive;
    }
}
