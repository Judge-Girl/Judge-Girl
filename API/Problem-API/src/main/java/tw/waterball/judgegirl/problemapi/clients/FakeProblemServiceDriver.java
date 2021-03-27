package tw.waterball.judgegirl.problemapi.clients;

import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

import java.util.HashMap;
import java.util.Map;

public class FakeProblemServiceDriver implements ProblemServiceDriver {

    private Map<Integer, ProblemView> problems = new HashMap<>();

    @Override
    public ProblemView getProblem(int problemId) throws NotFoundException {
        if (!problems.containsKey(problemId)) {
            throw new NotFoundException();
        }
        return problems.get(problemId);
    }

    public ProblemView addProblemView(ProblemView problemView) {
        problems.put(problemView.getId(), problemView);
        return problemView;
    }

    @Override
    public FileResource downloadProvidedCodes(int problemId, String languageEnvName, String providedCodesFileId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileResource downloadTestCaseIOs(int problemId, String testcaseIOsFileId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
