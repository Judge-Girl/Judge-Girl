package tw.waterball.judgegirl.commons.utils.functional;

import java.util.function.Consumer;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Otherwise<T> {
    private final T t;

    private Otherwise(T t) {
        this.t = t;
    }

    public static <T> Otherwise<T> of(T t) {
        return new Otherwise<>(t);
    }

    public static <T> Otherwise<T> empty() {
        return new Otherwise<>(null);
    }

    public void otherwise(Consumer<T> consumer) {
        if (t != null) {
            consumer.accept(this.t);
        }
    }
}
