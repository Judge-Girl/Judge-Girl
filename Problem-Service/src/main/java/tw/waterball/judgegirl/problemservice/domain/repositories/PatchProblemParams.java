package tw.waterball.judgegirl.problemservice.domain.repositories;

import lombok.Builder;
import org.jetbrains.annotations.Nullable;
import tw.waterball.judgegirl.entities.problem.JudgePluginTag;

import java.util.Optional;
import java.util.Set;

@Builder
public class PatchProblemParams {
    @Nullable
    private final String title;
    @Nullable
    private final String description;
    @Nullable
    private final JudgePluginTag matchPolicyPluginTag;
    @Nullable
    private final Set<JudgePluginTag> filterPluginTags;

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Optional<JudgePluginTag> getMatchPolicyPluginTag() {
        return Optional.ofNullable(matchPolicyPluginTag);
    }

    public Optional<Set<JudgePluginTag>> getFilterPluginTags() {
        return Optional.ofNullable(filterPluginTags);
    }
}
