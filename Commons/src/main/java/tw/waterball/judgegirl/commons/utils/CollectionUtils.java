package tw.waterball.judgegirl.commons.utils;

import java.util.Collection;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class CollectionUtils {
    @SafeVarargs
    public static <T> Collection<T> merge(Collection<T>... collection) {
        return stream(collection)
                .flatMap(Collection::stream)
                .collect(toList());
    }
}
