package tw.waterball.judgegirl.commons.utils;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.copyOf;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class MapUtils {

    public static <K, V> Map<K, V> zip(K[] keys, V[] values) {
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    @SafeVarargs
    public static <K> MapBuilder<K> map(K... keys) {
        return new MapBuilder<>(keys);
    }

    public static class MapBuilder<K> {
        private final K[] keys;

        public MapBuilder(K[] keys) {
            this.keys = copyOf(keys, keys.length);
        }

        @SafeVarargs
        public final <V> Map<K, V> to(V... values) {
            return zip(keys, values);
        }

    }
}
