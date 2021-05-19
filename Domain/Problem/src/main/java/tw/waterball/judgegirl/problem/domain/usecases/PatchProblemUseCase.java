package tw.waterball.judgegirl.problem.domain.usecases;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.problem.JudgePluginTag;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.problem.Testcase;
import tw.waterball.judgegirl.problem.domain.repositories.PatchProblemParams;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

@Named
public class PatchProblemUseCase extends BaseProblemUseCase {
    public PatchProblemUseCase(ProblemRepository problemRepository) {
        super(problemRepository);
    }

    public void execute(Request request) throws NotFoundException {
        if (problemRepository.problemExists(request.problemId)) {
            problemRepository.patchProblem(
                    request.problemId,
                    PatchProblemParams.builder()
                            .title(request.title)
                            .description(request.description)
                            .matchPolicyPluginTag(request.matchPolicyPluginTag)
                            .filterPluginTags(request.filterPluginTags)
                            .languageEnv(request.languageEnv)
                            .testcase(request.testcase)
                            .visible(request.visible)
                            .tags(request.tags)
                            .build());
        } else {
            throw notFound(Problem.class).id(request.problemId);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        public static final int NOT_PRESENT = -1;
        public int problemId = NOT_PRESENT;
        public String title;
        public String description;
        public JudgePluginTag matchPolicyPluginTag;
        public Collection<JudgePluginTag> filterPluginTags;
        public LanguageEnv languageEnv;
        public Testcase testcase;
        public boolean visible;
        public List<String> tags;

    }
}
