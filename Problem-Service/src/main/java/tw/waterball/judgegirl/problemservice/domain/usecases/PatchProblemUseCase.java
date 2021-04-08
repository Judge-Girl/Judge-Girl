package tw.waterball.judgegirl.problemservice.domain.usecases;

import lombok.Value;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.problem.JudgePluginTag;
import tw.waterball.judgegirl.problemservice.domain.repositories.PatchProblemParams;
import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemRepository;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
            throw notFound("problem").id(request.problemId);
        }
    }

    @Value
    public static class Request {
        public int problemId;
        public String title;
        public String description;
        public JudgePluginTag judgePluginTag;
        public Set<JudgePluginTag> filterPluginTags;

        public Request(int problemId, String title, String description, JudgePluginTag judgePluginTag, Collection<JudgePluginTag> filterPluginTags) {
            this.problemId = problemId;
            this.title = title;
            this.description = description;
            this.judgePluginTag = judgePluginTag;
            this.filterPluginTags = new HashSet<>(filterPluginTags);
        }
    }
}
