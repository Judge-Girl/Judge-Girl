package tw.waterball.judgegirl.commons.utils;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public abstract class StreamUtils {
    public static <T, R> List<T> mapToList(Collection<R> collection, Function<R, T> mapping) {
        return collection.stream().map(mapping).collect(Collectors.toList());
    }
}
