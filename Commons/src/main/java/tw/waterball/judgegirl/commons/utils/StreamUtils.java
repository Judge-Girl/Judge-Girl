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
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.range;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public abstract class StreamUtils {
    public static <T, R> List<T> flatMapToList(R[] array, ErrFunction<? super R, ? extends Stream<? extends T>> flatMapping) {
        return stream(array).flatMap(r -> {
            try {
                return flatMapping.apply(r);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(toList());
    }

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

    public static <T extends Comparable<T>> List<T> sortToList(Collection<T> collection) {
        return collection.stream().sorted().collect(toList());
    }

    public static <T> List<T> sortToList(Collection<T> collection, Comparator<T> comparator) {
        return collection.stream().sorted(comparator).collect(toList());
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

    public static <T, L, R> List<T> zipToList(Collection<Map.Entry<L, R>> entrySet,
                                              BiFunction<L, R, T> zipAndMap) {
        return mapToList(entrySet, entry -> zipAndMap.apply(entry.getKey(), entry.getValue()));
    }

    public static <T, L, R> List<T> zipToList(Map<L, R> map,
                                              BiFunction<L, R, T> zipAndMap) {
        return zipToList(map.entrySet(), zipAndMap);
    }

    public static <T, L, R> List<T> zipToList(L[] left, R[] right,
                                              BiFunction<L, R, T> zipAndMap) {
        return range(0, left.length)
                .mapToObj(i -> zipAndMap.apply(left[i], right[i]))
                .collect(toList());
    }

    public static <T, L, R> List<T> zipToList(Collection<L> left, Collection<R> right,
                                              BiPredicate<L, R> matching,
                                              BiFunction<L, R, T> zipAndMap) {
        var list = new ArrayList<T>(left.size() * right.size());
        for (L l : left) {
            for (R r : right) {
                if (matching.test(l, r)) {
                    list.add(zipAndMap.apply(l, r));
                }
            }
        }
        return list;
    }

    public static <T, L, R> List<T> zipToList(List<L> left, List<R> right,
                                              BiFunction<L, R, T> zipAndMap) {
        return range(0, left.size())
                .mapToObj(i -> zipAndMap.apply(left.get(i), right.get(i)))
                .collect(toList());
    }

    public static <T, L, R> List<T> zipToList(List<L> left, Function<L, R> mapper,
                                              BiFunction<L, R, T> zipAndMap) {
        return mapToList(left, l -> zipAndMap.apply(l, mapper.apply(l)));
    }

    public static <T> Optional<T> findFirst(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).findFirst();
    }

    public static <T> List<T> filterToList(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).collect(toList());
    }


    public static <T, K> Map<K, List<T>> groupingBy(Collection<T> collection, Function<? super T, ? extends K> classifier) {
        return collection.stream().collect(Collectors.groupingBy(classifier));
    }

    public static double average(Integer... nums) {
        return stream(nums).mapToInt(Integer::intValue).average().orElse(0);
    }

    public static <T> double average(Collection<T> collection, ToIntFunction<T> mapper) {
        return collection.stream().mapToInt(mapper).average().orElse(0);
    }

    public static int sum(Integer[] array) {
        return stream(array).mapToInt(Integer::intValue).sum();
    }

    public static int sum(Collection<Integer> collection) {
        return collection.stream().mapToInt(Integer::intValue).sum();
    }

    public static <T> int sum(Collection<T> collection, ToIntFunction<T> mapper) {
        return collection.stream().mapToInt(mapper).sum();
    }

    public static <T> long sum(Collection<T> collection, ToLongFunction<T> mapper) {
        return collection.stream().mapToLong(mapper).sum();
    }

    public static <T> List<T> generate(int count, T obj) {
        return range(0, count).mapToObj(i -> obj).collect(toList());
    }

    public static <T> List<T> generate(int count, IntFunction<T> generator) {
        return range(0, count).mapToObj(generator).collect(toList());
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

    public static <T> String join(List<T> ids, String delimiter) {
        return ids.stream().map(String::valueOf).collect(joining(delimiter));
    }
}
