package tw.waterball.judgegirl.problem.domain.repositories;

import lombok.Builder;
import org.jetbrains.annotations.Nullable;
import tw.waterball.judgegirl.primitives.problem.JudgePluginTag;

import java.util.Collection;
import java.util.Optional;

@Builder
public class PatchProblemParams {
    @Nullable
    private final String title;
    @Nullable
    private final String description;
    @Nullable
    private final JudgePluginTag matchPolicyPluginTag;
    @Nullable
    private final Collection<JudgePluginTag> filterPluginTags;

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Optional<JudgePluginTag> getMatchPolicyPluginTag() {
        return Optional.ofNullable(matchPolicyPluginTag);
    }

    public Optional<Collection<JudgePluginTag>> getFilterPluginTags() {
        return Optional.ofNullable(filterPluginTags);
    }
}
