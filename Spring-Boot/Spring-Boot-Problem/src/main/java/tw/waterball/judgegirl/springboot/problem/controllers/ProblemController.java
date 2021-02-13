/*
 *  Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tw.waterball.judgegirl.springboot.problem.controllers;

import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.problem.Testcase;
import tw.waterball.judgegirl.problemapi.views.ProblemItem;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemQueryParams;
import tw.waterball.judgegirl.problemservice.domain.usecases.*;
import tw.waterball.judgegirl.springboot.utils.ResponseEntityUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@CrossOrigin
@RestController
@RequestMapping("/api/problems")
@AllArgsConstructor
public class ProblemController {
    private final GetProblemUseCase getProblemUseCase;
    private final GetProblemListUseCase getProblemListUseCase;
    private final DownloadProvidedCodesUseCase downloadProvidedCodesUseCase;
    private final DownloadTestCaseIOsUseCase downloadTestCaseIOsUseCase;
    private final GetAllTagsUseCase getAllTagsUseCase;
    private final GetTestCasesUseCase getTestCasesUseCase;

    @GetMapping("/tags")
    public List<String> getTags() {
        return getAllTagsUseCase.execute();
    }

    @GetMapping
    public List<ProblemItem> getProblems(@RequestParam(value = "tags", required = false) String tagsSplitByCommas,
                                         @RequestParam(value = "page", defaultValue = "0") int page) {
        String[] tags = tagsSplitByCommas == null ? new String[0] : tagsSplitByCommas.split("\\s*,\\s*");
        GetProblemListPresenter presenter = new GetProblemListPresenter();
        getProblemListUseCase.execute(new ProblemQueryParams(tags, page), presenter);
        return presenter.present();
    }


    @GetMapping("/{problemId}")
    public ProblemView getProblem(@PathVariable int problemId) {
        GetProblemPresenter presenter = new GetProblemPresenter();
        getProblemUseCase.execute(new GetProblemUseCase.Request(problemId), presenter);
        return presenter.present();
    }


    @GetMapping(value = "/{problemId}/{langEnvName}/providedCodes/{providedCodesFileId}",
            produces = "application/zip")
    public ResponseEntity<InputStreamResource> downloadZippedProvidedCodes(@PathVariable int problemId,
                                                                      @PathVariable String langEnvName,
                                                                      @PathVariable String providedCodesFileId) {
        FileResource fileResource = downloadProvidedCodesUseCase.execute(
                new DownloadProvidedCodesUseCase.Request(problemId, langEnvName, providedCodesFileId));
        return ResponseEntityUtils.respondInputStreamResource(fileResource);
    }


    @GetMapping("/{problemId}/testcases")
    public List<Testcase> getTestCases(@PathVariable int problemId) {
        GetTestCasesPresenter presenter = new GetTestCasesPresenter();
        getTestCasesUseCase.execute(new GetTestCasesUseCase.Request(problemId), presenter);
        return presenter.present();
    }


    @GetMapping(value = "/{problemId}/testcaseIOs/{testcaseIOsFileId}",
            produces = "application/zip")
    public ResponseEntity<InputStreamResource> downloadZippedTestCaseInputs(@PathVariable int problemId,
                                                                            @PathVariable String testcaseIOsFileId) {
        FileResource fileResource = downloadTestCaseIOsUseCase
                .execute(new DownloadTestCaseIOsUseCase.Request(problemId, testcaseIOsFileId));
        return ResponseEntityUtils.respondInputStreamResource(fileResource);
    }
}

class GetProblemPresenter implements GetProblemUseCase.Presenter {
    private Problem problem;

    @Override
    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    ProblemView present() {
        return ProblemView.fromEntity(problem);
    }
}


class GetProblemListPresenter implements GetProblemListUseCase.Presenter {
    private List<Problem> problems;

    @Override
    public void setProblemList(List<Problem> problems) {
        this.problems = problems;
    }

    List<ProblemItem> present() {
        return problems.stream().map(ProblemItem::fromEntity)
                .collect(Collectors.toList());
    }
}

class GetTestCasesPresenter implements GetTestCasesUseCase.Presenter {
    private List<Testcase> testcases;

    @Override
    public void setTestcases(List<Testcase> testcases) {
        this.testcases = testcases;
    }

    List<Testcase> present() {
        return testcases;
    }
}