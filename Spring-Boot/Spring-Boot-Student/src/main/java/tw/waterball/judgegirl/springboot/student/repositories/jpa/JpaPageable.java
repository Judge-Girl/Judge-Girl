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

package tw.waterball.judgegirl.springboot.student.repositories.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
public class JpaPageable implements Pageable {
    private final int skip;
    private final int size;

    public JpaPageable(int skip, int size) {
        this.skip = skip;
        this.size = size;
    }

    @Override
    public int getPageNumber() {
        return 0;
    }

    @Override
    public int getPageSize() {
        return this.size;
    }

    @Override
    public long getOffset() {
        return this.skip;
    }

    @Override
    public Sort getSort() {
        return Sort.unsorted();
    }

    @Override
    public Pageable next() {
        return null;
    }

    @Override
    public Pageable previousOrFirst() {
        return this.first();
    }

    @Override
    public Pageable first() {
        return new JpaPageable(0, this.size);
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }
}
