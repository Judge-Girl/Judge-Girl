package tw.waterball.judgegirl.submission.domain.usecases.query;

import lombok.AllArgsConstructor;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@AllArgsConstructor
public class SortBy {
    private final String fieldName;
    private final boolean ascending;

    public static SortBy ascending(String fieldName) {
        return new SortBy(fieldName, true);
    }

    public static SortBy descending(String fieldName) {
        return new SortBy(fieldName, false);
    }
    
    public String getFieldName() {
        return fieldName;
    }

    public boolean isAscending() {
        return ascending;
    }
}
