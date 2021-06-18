package tw.waterball.judgegirl.primitives.submission;

import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNullElse;
import static java.util.Optional.ofNullable;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Bag extends HashMap<String, String> {

    private static final long serialVersionUID = 1L;
    
    public Bag() {
    }

    public Bag(String key, String val) {
        this(singletonMap(key, val));
    }

    public Bag(@Nullable Map<? extends String, ? extends String> m) {
        super(requireNonNullElse(m, emptyMap()));
    }

    public Bag(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public Bag(int initialCapacity) {
        super(initialCapacity);
    }

    public static Bag empty() {
        return new Bag(emptyMap());
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

}