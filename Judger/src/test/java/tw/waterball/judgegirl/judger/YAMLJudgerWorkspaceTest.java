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

import org.junit.jupiter.api.Test;
import tw.waterball.judgegirl.judger.layout.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class YAMLJudgerWorkspaceTest {

    @Test
    public void test() throws IOException {
        YAMLJudgerWorkspace workspace = new YAMLJudgerWorkspace("/judger-workspace.test.yaml");
        assertEquals("/judger", workspace.getPath().toString());
        assertEquals("/judger/log", workspace.getLogHomePath().toString());
        assertEquals("/judger/run/profiler", workspace.getProfilerPath().toString());

        SubmissionHome submissionHome = workspace.getSubmissionHome("s");
        assertEquals("/judger/run/s", submissionHome.getPath().toString());

        CompileHome compileHome = submissionHome.getCompileHome();
        assertEquals("/judger/run/s/compile", compileHome.getPath().toString());
        assertEquals("/judger/run/s/compile/compile.sh", compileHome.getCompileScriptPath().toString());
        assertEquals("/judger/run/s/compile/a.out", compileHome.getExecutablePath().toString());

        SourceRoot sourceRoot = compileHome.getSourceRoot();
        assertEquals("/judger/run/s/compile/src", sourceRoot.getPath().toString());

        TestcaseHome testcaseHome = submissionHome.getTestCaseHome("t");
        assertEquals("/judger/run/s/t", testcaseHome.getPath().toString());

        SandboxRoot sandboxRoot = testcaseHome.getSandboxRoot();
        assertEquals("/judger/run/s/t/in", sandboxRoot.getPath().toString());
        assertEquals("/judger/run/s/t/in/std.in", sandboxRoot.getStandardInPath().toString());
        assertEquals("/judger/run/s/t/in/std.out", sandboxRoot.getActualStandardOutPath().toString());
        assertEquals("/judger/run/s/t/in/std.err", sandboxRoot.getActualStandardErrPath().toString());
        assertEquals("/judger/run/s/t/in/a.out", sandboxRoot.getExecutablePath().toString());

        TestCaseOutputHome testCaseOutputHome = testcaseHome.getTestcaseOutputHome();
        assertEquals("/judger/run/s/t/out", testCaseOutputHome.getPath().toString());
        assertEquals("/judger/run/s/t/out/std.out", testCaseOutputHome.getExpectedStandardOutPath().toString());
    }

}