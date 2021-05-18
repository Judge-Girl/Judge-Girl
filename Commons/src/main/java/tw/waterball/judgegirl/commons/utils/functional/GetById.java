package tw.waterball.judgegirl.commons.utils.functional;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@FunctionalInterface
public interface GetById<ID, T> {
    T get(ID id);
}
