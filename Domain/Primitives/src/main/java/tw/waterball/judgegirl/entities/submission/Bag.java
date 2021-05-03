package tw.waterball.judgegirl.entities.submission;

import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNullElse;

/**
 * Submission's Bag to transfer additional messages.
 * Note: All of the messages (key, value) in the bag will be converted to lower-case.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Bag extends HashMap<String, String> {

    public static Bag empty() {
        return new Bag(emptyMap());
    }

    public Bag() {
        super();
    }

    public Bag(String key, String val) {
        this(singletonMap(key, val));
    }

    public Bag(@Nullable Map<? extends String, ? extends String> m) {
        super(requireNonNullElse(m, emptyMap()));
        makeAllKeysAndValuesLowercase();
    }

    public Bag(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public Bag(int initialCapacity) {
        super(initialCapacity);
    }

    public void makeAllKeysAndValuesLowercase() {
        var entries = new ArrayList<>(entrySet());
        for (var entry : entries) {
            remove(entry.getKey());
            put(entry.getKey().toLowerCase(), entry.getValue().toLowerCase());
        }
    }

    public Optional<String> getAsString(String key) {
        var value = get(key.toLowerCase());
        return Optional.ofNullable(value);
    }

    public OptionalInt getAsInteger(String key) {
        var value = get(key.toLowerCase());

        return value == null ? OptionalInt.empty() : OptionalInt.of(parseInt(value));
    }

    public OptionalLong getAsLong(String key) {
        var value = get(key.toLowerCase());
        return value == null ? OptionalLong.empty() : OptionalLong.of(parseLong(value));
    }

}
