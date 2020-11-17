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
import tw.waterball.judgegirl.entities.problem.JudgePluginTag;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.problem.Testcase;
import tw.waterball.judgegirl.entities.submission.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@SuppressWarnings("WeakerAccess")
public abstract class Judger {
    protected JudgeContext context;

    public void judge(int studentId, int problemId, String submissionId) throws IOException {
        retrieveEntities(studentId, problemId, submissionId);
        setupJudgerFileLayout();
        downloadProvidedCodes();
        downloadSubmittedCodes();
        downloadTestcaseIOs();

        CompileResult compileResult = CompileResult.success();
        if (isCompiledLanguage()) {
            compileResult = doCompile();
        }

        Verdict verdict;
        if (compileResult.isSuccessful()) {
            List<Judge> judges = runAndJudgeAllTestcases();
            verdict = new Verdict(judges);
            inspectCodeQuality(verdict);
        } else {
            verdict = issueCompileErrorVerdict(compileResult);
        }

        publishVerdict(verdict);
    }

    private void retrieveEntities(int studentId, int problemId, String submissionId) {
        Problem problem = findProblemById(problemId);
        List<Testcase> testcases = findTestcasesByProblemId(problemId);
        Submission submission = findSubmissionByIds(studentId, problemId, submissionId);
        this.context = new JudgeContext(studentId, problem, testcases, submission);
    }

    protected abstract Problem findProblemById(int problemId);

    protected abstract List<Testcase> findTestcasesByProblemId(int problemId);

    protected abstract Submission findSubmissionByIds(int problemId, int studentId, String submissionId);

    protected abstract void setupJudgerFileLayout();

    protected abstract void downloadProvidedCodes() throws IOException;

    protected abstract void downloadSubmittedCodes() throws IOException;

    protected abstract void downloadTestcaseIOs() throws IOException;

    protected abstract CompileResult doCompile();

    protected Verdict issueCompileErrorVerdict(CompileResult compileResult) {
        List<Judge> judges = getTestcases().stream()
                .map((testcase) -> new Judge(
                        testcase.getName(), JudgeStatus.CE,
                        ProgramProfile.onlyCompileError(compileResult.getErrorMessage())
                        , 0))
                .collect(Collectors.toList());

        return Verdict.compileError(compileResult.getErrorMessage(), judges);
    }

    protected abstract void onBeforeRunningTestcase(Testcase testcase);

    protected List<Judge> runAndJudgeAllTestcases() {
        return getTestcases().stream()
                .map(testcase -> {
                    onBeforeRunningTestcase(testcase);
                    ProgramExecutionResult executionResult = runTestcase(testcase);
                    return judgeFromProgramExecutionResult(testcase, executionResult);
                }).collect(Collectors.toList());
    }

    protected abstract ProgramExecutionResult runTestcase(Testcase testcase);

    protected Judge judgeFromProgramExecutionResult(Testcase testcase, ProgramExecutionResult executionResult) {
        if (executionResult.isSuccessful()) {
            if (isProgramOutputAllCorrect(testcase)) {
                return new Judge(testcase.getName(), JudgeStatus.AC,
                        executionResult.getProfile(), testcase.getGrade());
            } else {
                return new Judge(testcase.getName(), JudgeStatus.WA,
                        executionResult.getProfile(), 0);
            }
        }
        JudgeStatus failureStatus = executionResult.getStatus().mapToJudgeStatus();
        return new Judge(testcase.getName(), failureStatus,
                executionResult.getProfile(), 0);
    }

    protected abstract boolean isProgramOutputAllCorrect(Testcase testcase);

    private void inspectCodeQuality(Verdict verdict) {
        getProblem().getCodeInspectionPluginTag()
                .map(this::doCodeInspection)
                .ifPresent(verdict::setCodeInspectionReport);
    }

    protected abstract CodeInspectionReport doCodeInspection(JudgePluginTag codeInspectionTag);

    protected abstract void publishVerdict(Verdict verdict);

    @Value
    @AllArgsConstructor
    public static class JudgeContext {

        public final int studentId;
        public final Problem problem;
        public final List<Testcase> testcases;
        public final Submission submission;
    }

    protected boolean isCompiledLanguage() {
        return context.getProblem().isCompiledLanguage();
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
}
