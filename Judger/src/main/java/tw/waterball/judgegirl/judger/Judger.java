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

import lombok.AllArgsConstructor;
import lombok.Value;
import tw.waterball.judgegirl.judger.infra.compile.CompileResult;
import tw.waterball.judgegirl.judger.infra.testexecutor.TestcaseExecutionResult;
import tw.waterball.judgegirl.primitives.problem.*;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.primitives.submission.verdict.Judge;
import tw.waterball.judgegirl.primitives.submission.verdict.Verdict;
import tw.waterball.judgegirl.primitives.submission.verdict.VerdictIssuer;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The root of the Judger, adopting template method to represent the Judge Flow.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
@SuppressWarnings("WeakerAccess")
public abstract class Judger {
    protected JudgeContext context;

    public void judge(int studentId, int problemId, String submissionId) {
        try {
            retrieveEntities(studentId, problemId, submissionId);
            setupJudgerFileLayout();
            // the download's order should be fixed
            downloadSubmittedCodes();
            downloadProvidedCodes();
            downloadTestcaseIOs();

            problemShouldContainAtLeastOneTestcase();

            CompileResult compileResult = CompileResult.success();
            if (isCompiledLanguage()) {
                compileResult = doCompile();
            }

            Verdict verdict;
            if (compileResult.isSuccessful()) {
                doSourceCodeFiltering();
                List<Judge> judges = runAndJudgeAllTestcases();
                VerdictIssuer verdictIssuer = VerdictIssuer.fromJudges(judges);
                doVerdictFiltering(verdictIssuer);
                verdict = verdictIssuer.issue();
            } else {
                verdict = issueCompileErrorVerdict(compileResult);
            }

            publishVerdict(verdict);
        } catch (Exception err) {
            err.printStackTrace();
            publishVerdict(issueSystemErrorVerdict(err));
        }
    }

    private void retrieveEntities(int studentId, int problemId, String submissionId) {
        Problem problem = findProblemById(problemId);
        List<Testcase> testcases = problem.getTestcases();
        Submission submission = findSubmissionByIds(studentId, problemId, submissionId);
        this.context = new JudgeContext(studentId, problem, testcases, submission);
        onJudgeContextSetup(this.context);
    }

    protected abstract Problem findProblemById(int problemId);

    protected abstract Submission findSubmissionByIds(int problemId, int studentId, String submissionId);

    protected abstract void onJudgeContextSetup(JudgeContext judgeContext);

    protected abstract void setupJudgerFileLayout();

    protected abstract void downloadProvidedCodes() throws IOException;

    protected abstract void downloadSubmittedCodes() throws IOException;

    protected abstract void downloadTestcaseIOs() throws IOException;

    protected void problemShouldContainAtLeastOneTestcase() {
        if (getProblem().getTestcases().isEmpty()) {
            throw new IllegalStateException("The problem does not contain any testcase.");
        }
    }

    protected abstract CompileResult doCompile();

    protected Verdict issueSystemErrorVerdict(Exception err) {
        int maxGrade = getProblem().getTotalGrade();
        return Verdict.systemError(err.getMessage(), maxGrade);
    }

    protected Verdict issueCompileErrorVerdict(CompileResult compileResult) {
        int maxGrade = getProblem().getTotalGrade();
        return Verdict.compileError(compileResult.getErrorMessage(), maxGrade);
    }

    protected abstract void onBeforeRunningTestcase(Testcase testcase);

    protected abstract void onAfterRunningTestcase(Testcase testcase);

    protected List<Judge> runAndJudgeAllTestcases() {
        return getTestcases().stream()
                .map(testcase -> {
                    onBeforeRunningTestcase(testcase);
                    TestcaseExecutionResult executionResult = runTestcase(testcase);
                    onAfterRunningTestcase(testcase);
                    return judgeFromProgramExecutionResult(testcase, executionResult);
                }).collect(Collectors.toList());
    }

    protected abstract TestcaseExecutionResult runTestcase(Testcase testcase);

    protected Judge judgeFromProgramExecutionResult(Testcase testcase, TestcaseExecutionResult executionResult) {
        if (executionResult.isSuccessful()) {
            if (isProgramOutputAllCorrect(testcase)) {
                return new Judge(testcase, JudgeStatus.AC,
                        executionResult.getProfile(), testcase.getGrade());
            } else {
                return new Judge(testcase, JudgeStatus.WA,
                        executionResult.getProfile(), 0);
            }
        }
        JudgeStatus failureStatus = executionResult.getStatus().mapToJudgeStatus();
        return new Judge(testcase, failureStatus,
                executionResult.getProfile(), 0);
    }

    protected abstract boolean isProgramOutputAllCorrect(Testcase testcase);

    private void doSourceCodeFiltering() {
        getProblem().getFilterPluginTags()
                .forEach(this::doSourceCodeFilteringForTag);
    }

    protected abstract void doSourceCodeFilteringForTag(JudgePluginTag pluginTag);

    private void doVerdictFiltering(VerdictIssuer verdictIssuer) {
        getProblem().getFilterPluginTags()
                .forEach(tag -> doVerdictFilteringForTag(verdictIssuer, tag));
    }

    protected abstract void doVerdictFilteringForTag(VerdictIssuer verdictIssuer, JudgePluginTag pluginTag);


    protected abstract void publishVerdict(Verdict verdict);

    protected boolean isCompiledLanguage() {
        return getLanguageEnv().isCompiledLanguage();
    }

    protected int getStudent() {
        return context.getStudentId();
    }

    protected Problem getProblem() {
        return context.getProblem();
    }

    protected List<Testcase> getTestcases() {
        return context.getTestcases();
    }

    protected Submission getSubmission() {
        return context.getSubmission();
    }

    protected LanguageEnv getLanguageEnv() {
        return getProblem().getLanguageEnv(getSubmission().getLanguageEnvName());
    }

    @Value
    @AllArgsConstructor
    public static class JudgeContext {
        public int studentId;
        public Problem problem;
        public List<Testcase> testcases;
        public Submission submission;
    }
}
