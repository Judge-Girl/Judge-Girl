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

package tw.waterball.judgegirl.commons.utils;

import tw.waterball.judgegirl.commons.utils.functional.ErrConsumer;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public abstract class StreamUtils {
    public static <T, R> List<T> mapToList(Collection<R> collection, Function<R, T> mapping) {
        return collection.stream().map(mapping).collect(Collectors.toList());
    }

    public static <T, L, R> List<T> zipToList(List<L> left, List<R> right,
                                              BiFunction<L, R, T> zipAndMap) {
        return IntStream.range(0, left.size())
                .mapToObj(i -> zipAndMap.apply(left.get(i), right.get(i)))
                .collect(Collectors.toList());
    }

    public static <T> void atTheSameTime(T[] array, ErrConsumer<T> consumer) {
        Arrays.stream(array)
                .parallel()
                .forEach(t -> {
                    try {
                        consumer.accept(t);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
