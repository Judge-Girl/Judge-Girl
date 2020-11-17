package tw.waterball.judgegirl.problemservice.domain.repositories;


import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ProblemQueryParams {
    public final static ProblemQueryParams NO_PARAMS = new ProblemQueryParams(null, null);

    private String[] tags;

    @Nullable
    private Integer page;

    public ProblemQueryParams(String[] tags, @Nullable Integer page) {
        this.tags = tags;
        this.page = page;
    }

    public String[] getTags() {
        return tags;
    }

    public Optional<Integer> getPage() {
        return Optional.ofNullable(page);
    }
}
