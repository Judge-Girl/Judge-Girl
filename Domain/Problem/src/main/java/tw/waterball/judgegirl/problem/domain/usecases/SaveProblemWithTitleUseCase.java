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

    public void execute(String title, Presenter presenter) {
        Problem problem = Problem.builder()
                .title(title)
                .outputMatchPolicyPluginTag(AllMatchPolicyPlugin.TAG)
                .build();
        presenter.setProblem(problemRepository.save(problem));
    }

    public interface Presenter {
        void setProblem(Problem problem);
    }

}
