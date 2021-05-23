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

package tw.waterball.judgegirl.judger;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.utils.ZipUtils;
import tw.waterball.judgegirl.judger.infra.compile.CompileResult;
import tw.waterball.judgegirl.judger.infra.compile.Compiler;
import tw.waterball.judgegirl.judger.infra.compile.CompilerFactory;
import tw.waterball.judgegirl.judger.infra.testexecutor.TestcaseExecutionResult;
import tw.waterball.judgegirl.judger.infra.testexecutor.TestcaseExecutor;
import tw.waterball.judgegirl.judger.infra.testexecutor.TestcaseExecutorFactory;
import tw.waterball.judgegirl.judger.layout.*;
import tw.waterball.judgegirl.plugins.api.JudgeGirlPluginLocator;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.problem.SubmittedCodeSpec;
import tw.waterball.judgegirl.primitives.problem.Testcase;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.primitives.submission.verdict.Verdict;
import tw.waterball.judgegirl.primitives.submission.verdict.VerdictIssuedEvent;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.clients.VerdictPublisher;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * TODO: some drunk codes that ruins my perfectionism, require some talents to refactor it!
 * The main problem is regarding to the multiple languages' supporting,
 * every language should have its judge-flow customizable and
 * the current template method's used doesn't support it well.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
@SuppressWarnings("WeakerAccess")
public class CCJudger extends PluginExtendedJudger {
    private static final Logger logger = LogManager.getLogger(CCJudger.class);
    public static final String TEMP_SUBMITTED_CODES_DIR_NAME = "tempSubmittedCodes";
    private static final String EXECUTABLE_NAME = "a.out";
    private final JudgerWorkspace judgerWorkspace;
    private final ProblemServiceDriver problemServiceDriver;
    private final SubmissionServiceDriver submissionServiceDriver;
    private final VerdictPublisher verdictPublisher;
    private final CompilerFactory compilerFactory;
    private final TestcaseExecutorFactory testcaseExecutorFactory;

    private Set<String> filesWithinSandboxRootOtherThanOutFiles;
    private Set<File> inFiles;
    private Set<File> actualOutFiles;

    public CCJudger(JudgerWorkspace judgerWorkspace,
                    JudgeGirlPluginLocator pluginLocator,
                    ProblemServiceDriver problemServiceDriver,
                    SubmissionServiceDriver submissionServiceDriver,
                    VerdictPublisher verdictPublisher,
                    CompilerFactory compilerFactory,
                    TestcaseExecutorFactory testcaseExecutorFactory) {
        super(pluginLocator);
        this.judgerWorkspace = judgerWorkspace;
        this.problemServiceDriver = problemServiceDriver;
        this.submissionServiceDriver = submissionServiceDriver;
        this.verdictPublisher = verdictPublisher;
        this.compilerFactory = compilerFactory;
        this.testcaseExecutorFactory = testcaseExecutorFactory;
    }

    @Override
    protected Problem findProblemById(int problemId) {
        var problem = problemServiceDriver.getProblem(problemId)
                .map(ProblemView::toEntity)
                .orElseThrow(() -> notFound(Problem.class).id(problemId));
        logger.info(problem);
        return problem;
    }

    @Override
    protected Submission findSubmissionByIds(int problemId, int studentId, String submissionId) {
        return SubmissionView.toEntity(submissionServiceDriver.getSubmission(studentId,
                problemId, submissionId));
    }

    @SneakyThrows
    @Override
    protected void setupJudgerFileLayout() {
        mkdirIfNotExists(judgerWorkspace.getLogHomePath());
    }

    @Override
    protected void downloadSubmittedCodes() throws IOException {
        SubmissionHome submissionHome = judgerWorkspace.getSubmissionHome(getSubmission().getId());
        try (FileResource zip = submissionServiceDriver.downloadSubmittedCodes(
                getProblem().getId(), getStudent(),
                getSubmission().getId(), getSubmission().getSubmittedCodesFileId()
        )) {
            ZipUtils.unzipToDestination(zip.getInputStream(), getSourceRootPath());
        }

        // Since we don't want the providedCodes stay in the source root after compilation (for some source code filtering reason),
        // here we copy the submitted codes into a temporary directory
        // for latter swapping back to override the source root.
        Path tempSubmittedCodesPath = submissionHome.getPath().resolve(TEMP_SUBMITTED_CODES_DIR_NAME);
        FileUtils.copyDirectory(getSourceRootPath().toFile(), tempSubmittedCodesPath.toFile());

        logger.info("<After downloadSubmittedCodes> Files under src: {}.", stream(requireNonNull(getSourceRootPath().toFile().listFiles())).map(f -> f.getName() + (f.isDirectory() ? "/" : "")).collect(Collectors.joining(",")));
    }

    @Override
    protected void downloadProvidedCodes() throws IOException {
        LanguageEnv languageEnv = getLanguageEnv();
        try (FileResource zip = problemServiceDriver.downloadProvidedCodes(
                getProblem().getId(), languageEnv.getName(), languageEnv.getProvidedCodesFileId())) {
            ZipUtils.unzipToDestination(zip.getInputStream(), getSourceRootPath());
        }

        logger.info("<After downloadProvidedCodes> Files under src: {}.", stream(requireNonNull(getSourceRootPath().toFile().listFiles())).map(f -> f.getName() + (f.isDirectory() ? "/" : "")).collect(Collectors.joining(",")));
    }

    @Override
    protected void downloadTestcaseIOs() throws IOException {
        SubmissionHome submissionHome = getSubmissionHome();
        FileResource zip = problemServiceDriver.downloadTestCaseIOs(
                getProblem().getId(), getProblem().getTestcaseIOsFileId());
        ZipUtils.unzipToDestination(zip.getInputStream(), submissionHome.getPath());
    }

    @Override
    @SneakyThrows
    protected CompileResult doCompile() {
        String script = getLanguageEnv().getCompilation().getScript();
        Files.write(getCompileScriptPath(), script.getBytes());
        logger.info("<After compile> Files under src: {}.", stream(requireNonNull(getSourceRootPath().toFile().listFiles())).map(f -> f.getName() + (f.isDirectory() ? "/" : "")).collect(Collectors.joining(",")));
        Compiler compiler = compilerFactory.create(getSourceRootPath());
        CompileResult result = compiler.compile(getLanguageEnv().getCompilation());
        if (result.isSuccessful()) {
            // bring the compiled executable out to the compileHome
            FileUtils.copyFile(getSourceRootPath().resolve(EXECUTABLE_NAME).toFile(),
                    getCompileHome().getExecutablePath().toFile());
        }
        remainOnlySubmittedCodesInSourceRoot();
        return result;
    }

    private void remainOnlySubmittedCodesInSourceRoot() throws IOException {
        // first delete the source root
        FileUtils.forceDelete(getSourceRootPath().toFile());
        // and then swap back the temporary submitted code's dir
        Path tempSubmittedCodesPath = getSubmissionHome().getPath().resolve(TEMP_SUBMITTED_CODES_DIR_NAME);
        FileUtils.copyDirectory(tempSubmittedCodesPath.toFile(), getSourceRootPath().toFile());
    }

    @Override
    @SneakyThrows
    protected void onBeforeRunningTestcase(Testcase testcase) {
        Path sandboxRootPath = getSandboxRoot(testcase).getPath();
        inFiles = filterInFilesFromSandboxRoot(sandboxRootPath);

        /*
          Ir order to filter for the out-files produced by the tested program,
          we need to pre-record the files in the sandbox root,
          and compare it after running the testcase.
          (The files-difference must be 'std.out', 'std.err' and out-files)
         */
        Path testcaseOutputHomePath = getTestcaseOutputHome(testcase).getPath().resolve("std.out");
        if (!Files.exists(testcaseOutputHomePath)) {
            Files.createFile(testcaseOutputHomePath);
        }
        filesWithinSandboxRootOtherThanOutFiles =
                generateFilesOtherThanOutFilesFromSandboxRoot(sandboxRootPath);

        copyExecutableIntoSandboxRoot(testcase);
        // TODO should also copy provided codes, this will fail in an interpreted language case
        copyInterpretedSubmittedCodesIntoSandboxRoot(testcase);
    }

    @NotNull
    private Set<String> generateFilesOtherThanOutFilesFromSandboxRoot(Path sandboxRootPath) {
        var files = stream(requireNonNull(sandboxRootPath.toFile().listFiles()))
                .map(File::getName).collect(Collectors.toSet());
        files.add("std.out");
        files.add("std.err");
        files.add(EXECUTABLE_NAME);
        return files;
    }

    @NotNull
    private Set<File> filterInFilesFromSandboxRoot(Path sandboxRootPath) {
        return stream(requireNonNull(sandboxRootPath.toFile().listFiles()))
                .filter(f -> !f.getName().equals("std.in") && !f.getName().equals(EXECUTABLE_NAME))
                .collect(Collectors.toSet());
    }

    private void copyExecutableIntoSandboxRoot(Testcase testcase) throws IOException {
        Path executablePath = getCompileHome().getExecutablePath();
        Path sandboxRootPath = getSandboxRoot(testcase).getPath();
        FileUtils.copyFile(executablePath.toFile(),
                sandboxRootPath.resolve(executablePath.getFileName()).toFile());
    }

    private void copyInterpretedSubmittedCodesIntoSandboxRoot(Testcase testcase) throws IOException {
        // the program might execute the interpreted codes from the sandbox root
        // hence we must copy them to there
        SandboxRoot sandboxRoot = getSandboxRoot(testcase);
        for (SubmittedCodeSpec submittedCodeSpec : getLanguageEnv().getSubmittedCodeSpecs()) {
            if (submittedCodeSpec.getFormat().isInterpretedLanguage()) {
                Path interpretedSubmittedCodePath = getCompileHome().getPath().resolve(submittedCodeSpec.getFileName());
                Path copyDestinationPath = sandboxRoot.getPath().resolve(submittedCodeSpec.getFileName());
                FileUtils.copyFile(interpretedSubmittedCodePath.toFile(), copyDestinationPath.toFile());
            }
        }
    }

    @Override
    protected TestcaseExecutionResult runTestcase(Testcase testcase) {
        TestcaseExecutor testcaseExecutor =
                testcaseExecutorFactory.create(getSubmission().getId(),
                        testcase, judgerWorkspace);
        return testcaseExecutor.executeProgramByProfiler(
                judgerWorkspace.getProfilerPath());
    }

    @Override
    protected void onAfterRunningTestcase(Testcase testcase) {
        actualOutFiles = new HashSet<>(
                asList(requireNonNull(getSandboxRoot(testcase).getPath().toFile().listFiles())));
        actualOutFiles.removeIf(f -> filesWithinSandboxRootOtherThanOutFiles.contains(f.getName()));
    }

    @Override
    protected Path getActualStandardOutputPath(Testcase testcase) {
        return getTestcaseHome(testcase)
                .getSandboxRoot().getActualStandardOutPath();
    }

    @Override
    protected Path getExpectStandardOutputPath(Testcase testcase) {
        return getTestcaseHome(testcase)
                .getTestcaseOutputHome().getExpectedStandardOutPath();
    }

    @Override
    protected Map<Path, Path> getActualToExpectOutputFilePathMap(Testcase testcase) {
        HashMap<Path, Path> mapping = new HashMap<>();
        TestCaseOutputHome testCaseOutputHome = getTestcaseOutputHome(testcase);
        for (File actualOutFile : actualOutFiles) {
            String name = actualOutFile.getName();
            mapping.put(actualOutFile.toPath(), testCaseOutputHome.getPath().resolve(name));
        }
        return mapping;
    }

    protected TestCaseOutputHome getTestcaseOutputHome(Testcase testcase) {
        return getTestcaseHome(testcase).getTestcaseOutputHome();
    }

    protected SandboxRoot getSandboxRoot(Testcase testcase) {
        return getTestcaseHome(testcase).getSandboxRoot();
    }

    protected TestcaseHome getTestcaseHome(Testcase testcase) {
        return getSubmissionHome().getTestCaseHome(testcase.getName());
    }

    @Override
    protected Path getSourceRootPath() {
        return getSourceRoot().getPath();
    }

    protected SourceRoot getSourceRoot() {
        return getCompileHome().getSourceRoot();
    }

    protected CompileHome getCompileHome() {
        return getSubmissionHome().getCompileHome();
    }

    protected Path getCompileScriptPath() {
        return getSubmissionHome().getCompileHome().getCompileScriptPath();
    }

    protected SubmissionHome getSubmissionHome() {
        return judgerWorkspace.getSubmissionHome(getSubmission().getId());
    }

    @Override
    protected void publishVerdict(Verdict verdict) {
        verdictPublisher.publish(
                new VerdictIssuedEvent(getProblem().getId(),
                        getProblem().getTitle(), getStudent(),
                        getSubmission().getId(),
                        verdict, getSubmission().getSubmissionTime(),
                        getSubmission().getBag()));
    }

    @SneakyThrows
    private void mkdirIfNotExists(Path directoryPath) {
        if (!Files.exists(directoryPath)) {
            FileUtils.forceMkdir(directoryPath.toFile());
        }
    }

}
