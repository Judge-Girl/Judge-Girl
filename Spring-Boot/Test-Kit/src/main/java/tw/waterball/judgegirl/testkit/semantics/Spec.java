package tw.waterball.judgegirl.testkit.semantics;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface Spec<T> {
    void verify(T t);
}
