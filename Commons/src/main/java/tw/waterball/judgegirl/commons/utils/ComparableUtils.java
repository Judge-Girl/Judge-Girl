package tw.waterball.judgegirl.commons.utils;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ComparableUtils {

    public static <T extends Comparable<T>> T betterAndNewer(T oldOne, T newOne) {
        return oldOne.compareTo(newOne) > 0 ? oldOne : newOne;
    }
}
