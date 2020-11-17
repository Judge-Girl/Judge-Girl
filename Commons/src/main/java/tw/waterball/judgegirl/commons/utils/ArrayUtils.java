package tw.waterball.judgegirl.commons.utils;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * @author johnny850807@gmail.com (Waterball))
 */
public class ArrayUtils {

    public static <T> boolean contains(T[] array, Predicate<T> predicate) {
        return array.length != 0 && Arrays.stream(array).anyMatch(predicate);
    }

    public static <T> boolean contains(T[] array, T t) {
        return array.length != 0 && Arrays.asList(array).contains(t);
    }
}
