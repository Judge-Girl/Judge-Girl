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
    private final Boolean queryArchive;

    private final boolean queryVisible;

    private final boolean queryInvisible;

    /**
     * @param tags           query the problems which have the tags
     * @param page           query the problems in the pages
     * @param queryArchive   query the archived problems
     * @param queryVisible   query the visible problems
     * @param queryInvisible query the invisible problems
     */
    public ProblemQueryParams(String[] tags, @Nullable Integer page, @Nullable Boolean queryArchive, boolean queryVisible, boolean queryInvisible) {
        this.tags = tags;
        this.page = page;
        this.queryArchive = queryArchive;
        this.queryVisible = queryVisible;
        this.queryInvisible = queryInvisible;
    }

    public String[] getTags() {
        return tags;
    }

    public Optional<Integer> getPage() {
        return ofNullable(page);
    }

    public Optional<Boolean> queryArchive() {
        return ofNullable(queryArchive);
    }

    public boolean queryVisible() {
        return queryVisible;
    }

    public boolean queryInvisible() {
        return queryInvisible;
    }
}
