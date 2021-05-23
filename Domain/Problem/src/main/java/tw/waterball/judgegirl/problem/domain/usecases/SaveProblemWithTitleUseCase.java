package tw.waterball.judgegirl.problem.domain.usecases;

import tw.waterball.judgegirl.plugins.impl.match.AllMatchPolicyPlugin;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;

import javax.inject.Named;

@Named
public class SaveProblemWithTitleUseCase extends BaseProblemUseCase {
    public SaveProblemWithTitleUseCase(ProblemRepository problemRepository) {
        super(problemRepository);
    }

    public int execute(String title) {
        Problem problem = Problem.builder()
                .title(title)
                .outputMatchPolicyPluginTag(AllMatchPolicyPlugin.TAG)
                .build();
        return problemRepository.save(problem).getId();
    }
}
