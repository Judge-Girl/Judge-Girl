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
import tw.waterball.judgegirl.commons.utils.functional.ErrFunction;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public abstract class StreamUtils {
    public static <T, R> List<T> flatMapToList(Collection<R> collection, ErrFunction<? super R, ? extends Stream<? extends T>> flatMapping) {
        return collection.stream()
                .flatMap(r -> {
                    try {
                        return flatMapping.apply(r);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).collect(toList());
    }

    public static <T, R> List<T> mapToList(R[] array, ErrFunction<R, T> mapping) {
        return mapToList(asList(array), mapping);
    }

    public static <T, R> List<T> mapToList(Collection<R> collection, ErrFunction<R, T> mapping) {
        return collection.stream().map(in -> {
            try {
                return mapping.apply(in);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(toList());
    }

    public static <T, R> Set<T> mapToSet(R[] array, ErrFunction<R, T> mapping) {
        return stream(array).map(in -> {
            try {
                return mapping.apply(in);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(toSet());
    }

    public static <T, R> Set<T> mapToSet(Collection<R> collection, ErrFunction<R, T> mapping) {
        return collection.stream().map(in -> {
            try {
                return mapping.apply(in);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(toSet());
    }

    public static <T, K, U> Map<K, U> toMap(Collection<T> collection, Function<? super T, ? extends K> keyMapper,
                                            Function<? super T, ? extends U> valueMapper) {
        return collection.stream().collect(Collectors.toMap(keyMapper, valueMapper));
    }

    public static <T, L, R> List<T> zipToList(List<L> left, List<R> right,
                                              BiFunction<L, R, T> zipAndMap) {
        return IntStream.range(0, left.size())
                .mapToObj(i -> zipAndMap.apply(left.get(i), right.get(i)))
                .collect(toList());
    }

    public static <T> Optional<T> findFirst(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).findFirst();
    }

    public static <T> List<T> filterToList(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).collect(toList());
    }

    public static <T> int sum(Collection<T> collection, ToIntFunction<T> mapper) {
        return collection.stream().mapToInt(mapper).sum();
    }

    public static <T> void atTheSameTime(T[] array, ErrConsumer<T> consumer) {
        stream(array)
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
