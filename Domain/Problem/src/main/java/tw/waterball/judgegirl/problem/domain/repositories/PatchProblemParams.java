package tw.waterball.judgegirl.problem.domain.repositories;

import lombok.Builder;
import org.jetbrains.annotations.Nullable;
import tw.waterball.judgegirl.primitives.problem.JudgePluginTag;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;
import tw.waterball.judgegirl.primitives.problem.Testcase;

import java.util.Collection;
import java.util.List;
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
    @Nullable
    private final LanguageEnv languageEnv;

    private final Testcase testcase;

    private final boolean visible;
    @Nullable
    private final List<String> tags;

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

    public Optional<LanguageEnv> getLanguageEnv() {
        return Optional.ofNullable(languageEnv);
    }

    public Optional<Testcase> getTestcase() {
        return Optional.ofNullable(testcase);
    }

    public Optional<List<String>> getTags() {
        return Optional.ofNullable(tags);
    }

    public Optional<Boolean> isVisible() {
        return Optional.of(visible);
    }
}
