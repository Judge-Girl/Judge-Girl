/*
 * Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package tw.waterball.judgegirl.springboot.problem.controllers;

import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.problem.Testcase;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemQueryParams;
import tw.waterball.judgegirl.problem.domain.usecases.*;
import tw.waterball.judgegirl.problemapi.views.ProblemItem;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.springboot.utils.ResponseEntityUtils;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static tw.waterball.judgegirl.problem.domain.usecases.UploadProvidedCodeUseCase.PROVIDED_CODE_MULTIPART_KEY_NAME;
import static tw.waterball.judgegirl.springboot.utils.MultipartFileUtils.convertMultipartFilesToFileResources;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/api/problems")
@AllArgsConstructor
public class ProblemController {
    private final GetProblemUseCase getProblemUseCase;
    private final GetProblemListUseCase getProblemListUseCase;
    private final DownloadProvidedCodesUseCase downloadProvidedCodesUseCase;
    private final DownloadTestCaseIOsUseCase downloadTestCaseIOsUseCase;
    private final GetAllTagsUseCase getAllTagsUseCase;
    private final GetTestCasesUseCase getTestCasesUseCase;
    private final SaveProblemWithTitleUseCase saveProblemWithTitleUseCase;
    private final PatchProblemUseCase patchProblemUseCase;
    private final ArchiveOrDeleteProblemUseCase deleteProblemUseCase;
    private final UploadProvidedCodeUseCase uploadProvidedCodeUseCase;


    @GetMapping("/tags")
    public List<String> getTags() {
        return getAllTagsUseCase.execute();
    }

    @GetMapping
    public List<ProblemItem> getProblems(@RequestParam(value = "tags", required = false) String[] tags,
                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                         @RequestParam(required = false) int[] ids) {
        GetProblemListPresenter presenter = new GetProblemListPresenter();
        if (nonNull(ids)) {
            getProblemListUseCase.execute(ids, presenter);
        } else {
            getProblemListUseCase.execute(new ProblemQueryParams(tags == null ? new String[0] : tags, page), presenter);
        }
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

    @PostMapping(consumes = "text/plain")
    public int saveProblemWithTitleAndGetId(@RequestBody String title) {
        return saveProblemWithTitleUseCase.execute(title);
    }

    @PatchMapping("/{problemId}")
    public void patchProblem(@PathVariable int problemId,
                             @RequestBody PatchProblemUseCase.Request request) {
        patchProblemUseCase.execute(request);
    }

    @DeleteMapping("/{problemId}")
    public void archiveOrDeleteProblem(@PathVariable int problemId) {
        deleteProblemUseCase.execute(problemId);
    }


    @PutMapping("/{problemId}/langEnv/{langEnv}")
    public void updateLanguageEnv(@PathVariable int problemId,
                                  @RequestBody LanguageEnv languageEnv) {
        PatchProblemUseCase.Request request = PatchProblemUseCase.Request.builder().problemId(problemId)
                .languageEnv(languageEnv).build();
        patchProblemUseCase.execute(request);
    }

    @PutMapping("/{problemId}/{langEnvName}/providedCodes")
    public String uploadProvidedCodes(@PathVariable int problemId,
                                      @PathVariable String langEnvName,
                                      @RequestParam(PROVIDED_CODE_MULTIPART_KEY_NAME) MultipartFile[] providedCodes) {
        UploadProvidedCodeUseCase.Request request =
                new UploadProvidedCodeUseCase.Request(problemId, Language.valueOf(langEnvName), convertMultipartFilesToFileResources(providedCodes));
        UploadProvidedCodesPresenter presenter = new UploadProvidedCodesPresenter();
        presenter.setLanguage(Language.valueOf(langEnvName));
        uploadProvidedCodeUseCase.execute(request, presenter);
        return presenter.present();
    }

    @PutMapping("/{problemId}/testcases/{testcaseId}")
    public void updateOrAddTestcase(@PathVariable int problemId,
                                    @PathVariable String testcaseId,
                                    @RequestBody Testcase testcase) {
        testcase.setId(testcaseId);
        testcase.setProblemId(problemId);
        PatchProblemUseCase.Request request = PatchProblemUseCase.Request.builder()
                .problemId(problemId)
                .testcase(testcase).build();

        patchProblemUseCase.execute(request);
    }


    class GetProblemPresenter implements GetProblemUseCase.Presenter {
        private Problem problem;

        @Override
        public void setProblem(Problem problem) {
            this.problem = problem;
        }

        ProblemView present() {
            return ProblemView.toViewModel(problem);
        }
    }


    class GetProblemListPresenter implements GetProblemListUseCase.Presenter {
        private List<Problem> problems;

        @Override
        public void showProblems(List<Problem> problems) {
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

    class UploadProvidedCodesPresenter implements UploadProvidedCodeUseCase.Presenter {
        private Problem problem;
        private Language language;

        @Override
        public void showResult(Problem problem) {
            this.problem = problem;
        }

        public void setLanguage(Language language) {
            this.language = language;
        }

        String present() {
            return problem.getLanguageEnv(language).getProvidedCodesFileId();
        }
    }
}


