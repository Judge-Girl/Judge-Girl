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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.problem.Testcase;
import tw.waterball.judgegirl.primitives.problem.TestcaseIO;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemQueryParams;
import tw.waterball.judgegirl.problem.domain.usecases.*;
import tw.waterball.judgegirl.problem.domain.usecases.PatchProblemUseCase.LanguageEnvUpsert;
import tw.waterball.judgegirl.problemapi.views.ProblemItem;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.problemapi.views.TestcaseView;
import tw.waterball.judgegirl.springboot.utils.ResponseEntityUtils;

import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;
import static tw.waterball.judgegirl.springboot.utils.MultipartFileUtils.convertMultipartFileToFileResource;
import static tw.waterball.judgegirl.springboot.utils.MultipartFileUtils.convertMultipartFilesToFileResources;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = "/api/problems")
@AllArgsConstructor
public class ProblemController {
    public static final String TESTCASE_IN_FILES_MULTIPART_KEY_NAME = "testcaseIOs.inFiles";
    public static final String TESTCASE_OUT_FILES_MULTIPART_KEY_NAME = "testcaseIOs.outFiles";
    public static final String TESTCASE_STDIN_MULTIPART_KEY_NAME = "testcaseIOs.stdIn";
    public static final String TESTCASE_STDOUT_MULTIPART_KEY_NAME = "testcaseIOs.stdOut";
    public static final String TESTCASE_DELETE_IN_FILES_MULTIPART_KEY_NAME = "testcaseIOs.inFiles.delete";
    public static final String TESTCASE_DELETE_OUT_FILES_MULTIPART_KEY_NAME = "testcaseIOs.outFiles.delete";
    public static final String PROVIDED_CODE_MULTIPART_KEY_NAME = "providedCodes";
    private final GetProblemUseCase getProblemUseCase;
    private final GetProblemsUseCase getProblemsUseCase;
    private final DownloadProvidedCodesUseCase downloadProvidedCodesUseCase;
    private final DownloadTestCaseIOsUseCase downloadTestCaseIOsUseCase;
    private final GetAllTagsUseCase getAllTagsUseCase;
    private final SaveProblemWithTitleUseCase saveProblemWithTitleUseCase;
    private final PatchProblemUseCase patchProblemUseCase;
    private final ArchiveOrDeleteProblemUseCase deleteProblemUseCase;
    private final UploadProvidedCodeUseCase uploadProvidedCodeUseCase;
    private final DeleteTestcaseUseCase deleteTestcaseUseCase;
    private final PatchTestcaseIOUseCase patchTestcaseIOUseCase;
    private final RestoreProblemUseCase restoreProblemUseCase;
    private final TokenService tokenService;

    @GetMapping("/tags")
    public List<String> getTags() {
        return getAllTagsUseCase.execute();
    }

    @GetMapping
    public List<?> getProblems(@RequestHeader(value = "Authorization", required = false) String authorization,
                               @RequestParam(value = "tags", required = false) String[] tags,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "visible", defaultValue = "true") boolean visible,
                               @RequestParam(value = "invisible", defaultValue = "true") boolean invisible,
                               @RequestParam(value = "archive", required = false) Boolean archive,
                               @RequestParam(required = false) int[] ids) {
        var token = tokenService.parseBearerTokenAndValidate(authorization);
        // only admin can view the invisible problems
        boolean includeInvisibleProblems = token.isAdmin();
        if (ids != null) {
            var presenter = new GetProblemsPresenter();
            getProblemsUseCase.execute(new GetProblemsUseCase.Request(includeInvisibleProblems, ids), presenter);
            return presenter.present();
        } else {
            // only admin can view the archived problems
            archive = archive == null ? null : archive && token.isAdmin();

            // only admin can view the invisible problems
            invisible = invisible && token.isAdmin();
            var presenter = new GetProblemItemsPresenter();
            getProblemsUseCase.execute(new ProblemQueryParams(tags == null ? new String[0] : tags, page, archive, visible, invisible), presenter);
            return presenter.present();
        }
    }

    @GetMapping("/{problemId}")
    public ProblemView getProblem(@RequestHeader(value = "Authorization", required = false) String authorization,
                                  @PathVariable int problemId) {
        var token = tokenService.parseBearerTokenAndValidate(authorization);
        boolean includeInvisibleProblem = token.isAdmin();
        var presenter = new GetProblemPresenter();
        getProblemUseCase.execute(new GetProblemUseCase.Request(problemId, includeInvisibleProblem), presenter);
        return presenter.present();
    }

    @GetMapping("/{problemId}/testcases")
    public List<Testcase> getTestCases(@PathVariable int problemId) {
        var presenter = new GetTestCasesPresenter();
        getProblemUseCase.execute(new GetProblemUseCase.Request(problemId), presenter);
        return presenter.present();
    }

    @DeleteMapping("/{problemId}/testcases/{testcaseId}")
    public void deleteTestcase(@RequestHeader("Authorization") String authorization,
                               @PathVariable int problemId,
                               @PathVariable String testcaseId) {
        tokenService.ifAdminToken(authorization,
                token -> deleteTestcaseUseCase.execute(new DeleteTestcaseUseCase.Request(problemId, testcaseId)));
    }

    @GetMapping(value = "/{problemId}/{langEnvName}/providedCodes/{providedCodesFileId}",
            produces = "application/zip")
    public ResponseEntity<InputStreamResource> downloadProvidedCodes(@RequestHeader("Authorization") String authorization,
                                                                     @PathVariable int problemId,
                                                                     @PathVariable String langEnvName,
                                                                     @PathVariable String providedCodesFileId) {
        return tokenService.returnIfAdmin(authorization, token -> {
            FileResource fileResource = downloadProvidedCodesUseCase
                    .execute(new DownloadProvidedCodesUseCase.Request(problemId, langEnvName, providedCodesFileId));
            return ResponseEntityUtils.respondInputStreamResource(fileResource);
        });
    }

    @GetMapping(value = "/{problemId}/testcases/{testcaseId}/io",
            produces = "application/zip")
    public ResponseEntity<InputStreamResource> downloadTestCaseIOs(@RequestHeader("Authorization") String authorization,
                                                                   @PathVariable int problemId,
                                                                   @PathVariable String testcaseId) {
        return tokenService.returnIfAdmin(authorization, token -> {
            FileResource fileResource = downloadTestCaseIOsUseCase
                    .execute(new DownloadTestCaseIOsUseCase.Request(problemId, testcaseId));
            return ResponseEntityUtils.respondInputStreamResource(fileResource);
        });
    }

    @PostMapping(consumes = "text/plain")
    public int saveProblemWithTitleAndGetId(@RequestHeader("Authorization") String authorization,
                                            @RequestBody String title) {
        return tokenService.returnIfAdmin(authorization, token ->
                saveProblemWithTitleUseCase.execute(title));
    }

    @PatchMapping("/{problemId}")
    public void patchProblem(@RequestHeader("Authorization") String authorization,
                             @PathVariable int problemId,
                             @RequestBody PatchProblemUseCase.Request request) {
        request.problemId = problemId;
        tokenService.ifAdminToken(authorization,
                token -> patchProblemUseCase.execute(request));
    }

    @DeleteMapping("/{problemId}")
    public void archiveOrDeleteProblem(@RequestHeader("Authorization") String authorization,
                                       @PathVariable int problemId) {
        tokenService.ifAdminToken(authorization,
                token -> deleteProblemUseCase.execute(problemId));
    }

    @PutMapping("/{problemId}/langEnv/{langEnv}")
    public void updateLanguageEnv(@RequestHeader("Authorization") String authorization,
                                  @PathVariable int problemId,
                                  @PathVariable String langEnv,
                                  @RequestBody LanguageEnvUpsert languageEnvUpsert) {
        if (!languageEnvUpsert.getName().equals(langEnv)) {
            throw new IllegalArgumentException("LangEnv does not match.");
        }
        tokenService.ifAdminToken(authorization, token -> {
            PatchProblemUseCase.Request request = PatchProblemUseCase.Request.builder().problemId(problemId)
                    .languageEnv(languageEnvUpsert).build();
            patchProblemUseCase.execute(request);
        });
    }

    @PutMapping("/{problemId}/{langEnvName}/providedCodes")
    public String uploadProvidedCodes(@RequestHeader("Authorization") String authorization,
                                      @PathVariable int problemId,
                                      @PathVariable String langEnvName,
                                      @RequestParam(PROVIDED_CODE_MULTIPART_KEY_NAME) MultipartFile[] providedCodes) {
        return tokenService.returnIfAdmin(authorization, token -> {
            var request = new UploadProvidedCodeUseCase.Request(problemId, Language.valueOf(langEnvName),
                    convertMultipartFilesToFileResources(providedCodes));
            var presenter = new UploadProvidedCodesPresenter();
            presenter.setLanguage(Language.valueOf(langEnvName));
            uploadProvidedCodeUseCase.execute(request, presenter);
            return presenter.present();
        });
    }

    @PatchMapping("/{problemId}/testcases/{testcaseId}/io")
    public TestcaseView patchTestcaseIO(@RequestHeader("Authorization") String authorization,
                                        @PathVariable int problemId,
                                        @PathVariable String testcaseId,
                                        @RequestParam(value = TESTCASE_DELETE_IN_FILES_MULTIPART_KEY_NAME, required = false) Part insDeletion,
                                        @RequestParam(value = TESTCASE_DELETE_OUT_FILES_MULTIPART_KEY_NAME, required = false) Part outsDeletion,
                                        @RequestParam(value = TESTCASE_STDIN_MULTIPART_KEY_NAME, required = false) MultipartFile stdIn,
                                        @RequestParam(value = TESTCASE_STDOUT_MULTIPART_KEY_NAME, required = false) MultipartFile stdOut,
                                        @RequestParam(TESTCASE_IN_FILES_MULTIPART_KEY_NAME) MultipartFile[] inFiles,
                                        @RequestParam(TESTCASE_OUT_FILES_MULTIPART_KEY_NAME) MultipartFile[] outFiles) throws IOException {
        String[] deletedIns = insDeletion == null ? new String[0] : IOUtils.toString(insDeletion.getInputStream(), StandardCharsets.UTF_8).split("\\s*,\\s*");
        String[] deletedOuts = outsDeletion == null ? new String[0] : IOUtils.toString(outsDeletion.getInputStream(), StandardCharsets.UTF_8).split("\\s*,\\s*");
        return tokenService.returnIfAdmin(authorization, token -> {
            var presenter = new PatchTestcaseIoPresenter(testcaseId);
            patchTestcaseIOUseCase.execute(new PatchTestcaseIOUseCase.Request(
                    problemId, testcaseId,
                    testcaseIoPatching(testcaseId, stdIn, stdOut, inFiles, outFiles, deletedIns, deletedOuts)), presenter);
            return presenter.present();
        });
    }

    private TestcaseIO.IoPatching testcaseIoPatching(String testcaseId, MultipartFile stdIn, MultipartFile stdOut, MultipartFile[] inFiles, MultipartFile[] outFiles, String[] deletedIns, String[] deletedOuts) {
        return new TestcaseIO.IoPatching(
                testcaseId, deletedIns, deletedOuts,
                stdIn == null ? null : convertMultipartFileToFileResource(stdIn),
                stdOut == null ? null : convertMultipartFileToFileResource(stdOut),
                Set.copyOf(convertMultipartFilesToFileResources(inFiles)),
                Set.copyOf(convertMultipartFilesToFileResources(outFiles))
        );
    }

    private TestcaseIO.Files collectToTestcaseIOFiles(String testcaseId, MultipartFile stdIn, MultipartFile stdOut,
                                                      MultipartFile[] inFiles, MultipartFile[] outFiles) {
        return new TestcaseIO.Files(testcaseId,
                convertMultipartFileToFileResource(stdIn),
                convertMultipartFileToFileResource(stdOut),
                new HashSet<>(convertMultipartFilesToFileResources(inFiles)),
                new HashSet<>(convertMultipartFilesToFileResources(outFiles)));
    }

    @PutMapping("/{problemId}/testcases/{testcaseId}")
    public void upsertTestcase(@RequestHeader("Authorization") String authorization,
                               @PathVariable int problemId,
                               @PathVariable String testcaseId,
                               @RequestBody PatchProblemUseCase.TestcaseUpsert testcase) {
        tokenService.ifAdminToken(authorization, token -> {
            testcase.setId(testcaseId);
            testcase.setProblemId(problemId);
            PatchProblemUseCase.Request request = PatchProblemUseCase.Request.builder()
                    .problemId(problemId)
                    .testcase(testcase).build();

            patchProblemUseCase.execute(request);
        });
    }

    @PatchMapping("/{problemId}/restore")
    public void restoreProblem(@RequestHeader("Authorization") String authorization,
                               @PathVariable int problemId) {
        tokenService.ifAdminToken(authorization,
                token -> restoreProblemUseCase.execute(problemId));
    }
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

class GetProblemItemsPresenter implements GetProblemsUseCase.Presenter {
    private List<Problem> problems;

    @Override
    public void showProblems(List<Problem> problems) {
        this.problems = problems;
    }

    List<ProblemItem> present() {
        return mapToList(problems, ProblemItem::toProblemItem);
    }
}

class GetProblemsPresenter implements GetProblemsUseCase.Presenter {
    private List<Problem> problems;

    @Override
    public void showProblems(List<Problem> problems) {
        this.problems = problems;
    }

    List<ProblemView> present() {
        return mapToList(problems, ProblemView::toViewModel);
    }
}

class GetTestCasesPresenter implements GetProblemUseCase.Presenter {
    private List<Testcase> testcases;

    @Override
    public void setProblem(Problem problem) {
        this.testcases = problem.getTestcases();
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

@RequiredArgsConstructor
class PatchTestcaseIoPresenter implements PatchTestcaseIOUseCase.Presenter {
    private Problem problem;
    private final String testcaseId;

    @Override
    public void showResult(Problem problem) {
        this.problem = problem;
    }

    TestcaseView present() {
        return problem.getTestcaseById(testcaseId)
                .map(TestcaseView::toViewModel)
                .orElseThrow(() -> new RuntimeException("Presentation error."));
    }
}
