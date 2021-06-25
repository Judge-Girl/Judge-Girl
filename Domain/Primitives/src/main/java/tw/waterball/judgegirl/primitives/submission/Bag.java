package tw.waterball.judgegirl.primitives.submission;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.Optional.ofNullable;
import static tw.waterball.judgegirl.commons.utils.MapUtils.map;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Bag implements Map<String, String> {

    private final Map<String, String> map;

    private static final long serialVersionUID = 1L;
    
    public Bag() {
        this(new HashMap<>());
    }

    public Bag(String key, String val) {
        this(map(key).to(val));
    }

    public Bag(@Nullable Map<String, String> m) {
        map = requireNonNullElseGet(m, HashMap::new);
    }

    public static Bag empty() {
        return ImmutableBag.EMPTY;
    }

    public Bag addEntry(String key, String value) {
        put(key, value);
        return this;
    }

    public Optional<String> getAsString(String key) {
        return ofNullable(get(key));
    }

    public OptionalInt getAsInteger(String key) {
        var value = get(key);
        return value == null ? OptionalInt.empty() : OptionalInt.of(parseInt(value));
    }

    public OptionalLong getAsLong(String key) {
        var value = get(key);
        return value == null ? OptionalLong.empty() : OptionalLong.of(parseLong(value));
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return map.get(key);
    }

    @Nullable
    @Override
    public String put(String key, String value) {
        return map.put(key, value);
    }

    @Override
    public String remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends String> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<String> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            if (o instanceof Map) {
                return map.equals(o);
            }
            return false;
        } else {
            Bag bag = (Bag) o;
            return map.equals(bag.map);
        }
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String getOrDefault(Object key, String defaultValue) {
        return map.getOrDefault((String) key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super String> action) {
        map.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super String, ? extends String> function) {
        map.replaceAll(function);
    }

    @Nullable
    @Override
    public String putIfAbsent(String key, String value) {
        return map.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return map.remove(key, value);
    }

    @Override
    public boolean replace(String key, String oldValue, String newValue) {
        return map.replace(key, oldValue, newValue);
    }

    @Nullable
    @Override
    public String replace(String key, String value) {
        return map.replace(key, value);
    }

    @Override
    public String computeIfAbsent(String key, @NotNull Function<? super String, ? extends String> mappingFunction) {
        return map.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public String computeIfPresent(String key, @NotNull BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
        return map.computeIfPresent(key, remappingFunction);
    }

    @Override
    public String compute(String key, @NotNull BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
        return map.compute(key, remappingFunction);
    }

    @Override
    public String merge(String key, @NotNull String value, @NotNull BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
        return map.merge(key, value, remappingFunction);
    }
}