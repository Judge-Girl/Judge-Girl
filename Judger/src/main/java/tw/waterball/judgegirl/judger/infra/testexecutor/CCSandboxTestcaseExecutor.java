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

package tw.waterball.judgegirl.judger.infra.testexecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tw.waterball.judgegirl.commons.helpers.process.AbstractProcessRunner;
import tw.waterball.judgegirl.judger.layout.JudgerWorkspace;
import tw.waterball.judgegirl.judger.layout.SandboxRoot;
import tw.waterball.judgegirl.judger.layout.TestcaseHome;
import tw.waterball.judgegirl.primitives.problem.Testcase;
import tw.waterball.judgegirl.primitives.submission.verdict.ProgramProfile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.lang.String.format;

/**
 * @author - Haribo, johnny850807@gmail.com (Waterball)
 */
public class CCSandboxTestcaseExecutor extends AbstractProcessRunner implements TestcaseExecutor {
    private static final Logger logger = LoggerFactory.getLogger(CCSandboxTestcaseExecutor.class);
    private static final int numOfArguments = 18;
    private static final String seccompRuleName = "general";
    private static final int uid = 65534;
    private static final int gid = 65534;
    private static final int memoryLimitCheckOnly = 0;
    public static final String JUDGER_LOG_FILE_FORMAT = "judger-submission-%s.log";
    private final TestcaseHome testCaseHome;
    private final Path logPath;
    private final String submissionId;
    private final int cpuTimeLimit;
    private final int realTimeLimit;
    private final long memoryLimit;
    private final long stackLimit;
    private final long outputLimit;
    private final int processLimit;

    public CCSandboxTestcaseExecutor(String submissionId,
                                     Testcase testcase,
                                     JudgerWorkspace layoutResolver) {
        this.submissionId = submissionId;
        this.logPath = layoutResolver.getLogHomePath().resolve(format(JUDGER_LOG_FILE_FORMAT, submissionId));
        this.testCaseHome = layoutResolver.getSubmissionHome(submissionId)
                .getTestCaseHome(testcase.getName());
        this.realTimeLimit = testcase.getTimeLimit();
        this.cpuTimeLimit = -1;
        this.memoryLimit = this.stackLimit = testcase.getMemoryLimit();
        this.outputLimit = testcase.getOutputLimit();
        this.processLimit = testcase.getThreadNumberLimit();
    }

    @Override
    public TestcaseExecutionResult executeProgramByProfiler(Path profilerPath) {
        String[] arguments = new String[numOfArguments];
        arguments[0] = profilerPath.toString();
        arguments[1] = String.valueOf(this.cpuTimeLimit);
        arguments[2] = String.valueOf(this.realTimeLimit);
        arguments[3] = String.valueOf(this.memoryLimit);
        arguments[4] = String.valueOf(this.stackLimit);
        arguments[5] = String.valueOf(this.processLimit);
        arguments[6] = String.valueOf(this.outputLimit);
        arguments[7] = String.valueOf(memoryLimitCheckOnly);

        SandboxRoot sandboxRoot = testCaseHome.getSandboxRoot();

        // Since the executable will be run under the sandbox root
        // the path must be relative, and './' must be appended as to turn it into a execution command
        // for example:  "./a.out"
        arguments[8] = Paths.get("./").resolve(sandboxRoot.getExecutablePath().getFileName()).toString();

        arguments[9] = sandboxRoot.getStandardInPath().toString();
        arguments[10] = sandboxRoot.getActualStandardOutPath().toString();
        arguments[11] = sandboxRoot.getActualStandardErrPath().toString();
        arguments[12] = logPath.toString();
        arguments[13] = sandboxRoot.getPath().toString();
        arguments[14] = seccompRuleName;
        arguments[15] = submissionId;
        arguments[16] = String.valueOf(uid);
        arguments[17] = String.valueOf(gid);

        logger.info("Profiler's arguments: {}.", Arrays.toString(arguments));

        try {
            runProcess(arguments);
            awaitTermination();
            return parseResult();
        } catch (IOException e) { // TODO Error Handling
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private TestcaseExecutionResult parseResult() {
        String processReturnString = getStdout();
        String[] processReturnArray = processReturnString.split(",");

        logger.debug("Parsing results from array: {}.", Arrays.toString(processReturnArray));

        long runtime = Integer.parseInt(processReturnArray[1]);
        long memory = Long.parseLong(processReturnArray[2]);
        int statusNumber = Integer.parseInt(processReturnArray[6]);
        TestcaseExecutionResult result = new TestcaseExecutionResult(getStatus(statusNumber),
                new ProgramProfile(runtime, memory, getStderr()));

        logger.info(result.toString());
        return result;
    }

    private TestcaseExecutionResult.Status getStatus(int resultNumber) {
        switch (resultNumber) {
            case 0:
            case 1: // Don't consider cpu time temporarily
                return TestcaseExecutionResult.Status.SUCCESSFUL;
            case 2:
                return TestcaseExecutionResult.Status.TIME_LIMIT_EXCEEDS;
            case 3:
                return TestcaseExecutionResult.Status.MEMORY_LIMIT_EXCEEDS;
            case 4:
                return TestcaseExecutionResult.Status.RUNTIME_ERROR;
            case 5:
                return TestcaseExecutionResult.Status.SYSTEM_ERROR;
            case 6:
                return TestcaseExecutionResult.Status.OUTPUT_LIMIT_EXCEEDS;
            default:
                throw new RuntimeException("Unreachable statement.");
        }
    }
}
