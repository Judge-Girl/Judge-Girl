package tw.waterball.judgegirl.problem.domain.usecases;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.problem.JudgePluginTag;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problem.domain.repositories.PatchProblemParams;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;

import javax.inject.Named;
import java.util.Collection;

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
                            .matchPolicyPluginTag(request.judgePluginTag)
                            .filterPluginTags(request.filterPluginTags).build());
        } else {
            throw notFound(Problem.class).id(request.problemId);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public static final int NOT_PRESENT = -1;
        public int problemId = NOT_PRESENT;
        public String title;
        public String description;
        public JudgePluginTag judgePluginTag;
        public Collection<JudgePluginTag> filterPluginTags;
    }
}
