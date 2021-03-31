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

package tw.waterball.judgegirl.springboot.helpers;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
public class SkipAndSizePageable implements Pageable, Serializable {

    private static final long serialVersionUID = -25822477129613575L;

    private final int skip;
    private final int size;
    private final Sort sort;

    public SkipAndSizePageable(int skip, int size, Sort sort) {
        validation(skip, size);
        this.skip = skip;
        this.size = size;
        this.sort = sort;
    }

    public SkipAndSizePageable(int skip, int size) {
        validation(skip, size);
        this.skip = skip;
        this.size = size;
        this.sort = Sort.unsorted();
    }

    private void validation(int skip, int size) {
        if (skip < 0) {
            throw new IllegalArgumentException("Offset index must not be less than zero!");
        } else if (size < 1) {
            throw new IllegalArgumentException("Limit must not be less than one!");
        }
    }

    @Override
    public int getPageNumber() {
        return skip / size;
    }

    @Override
    public int getPageSize() {
        return this.size;
    }

    @Override
    public long getOffset() {
        return this.skip;
    }

    @NotNull
    @Override
    public Sort getSort() {
        return this.sort;
    }

    @NotNull
    @Override
    public Pageable next() {
        return new SkipAndSizePageable(this.skip + this.size, this.size, this.sort);
    }

    @NotNull
    @Override
    public Pageable previousOrFirst() {
        return this.hasPrevious() ? this.previous() : this.first();
    }

    @NotNull
    @Override
    public Pageable first() {
        return new SkipAndSizePageable(0, this.size, this.sort);
    }

    private Pageable previous() {
        return new SkipAndSizePageable(this.skip - this.size, this.size, this.sort);
    }

    @Override
    public boolean hasPrevious() {
        return getPageNumber() != 0;
    }
}
