package tw.waterball.judgegirl.primitives.submission;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ImmutableBag extends Bag {
    public static final Bag EMPTY = new ImmutableBag();

    private ImmutableBag() {
        this.map = Map.of();
    }

    public ImmutableBag(@Nullable Map<String, String> map) {
        this.map = map == null ? Map.of() : Map.copyOf(map);
    }

}
