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
import tw.waterball.judgegirl.judger.filelayout.Directory;
import tw.waterball.judgegirl.judger.filelayout.FileLayout;
import tw.waterball.judgegirl.judger.filelayout.YAMLFileLayoutBuilder;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class YAMLJudgerWorkspace implements JudgerWorkspace {
    private FileLayout fileLayout;
    private Directory root;

    public YAMLJudgerWorkspace(String yamlResourcePath) throws IOException {
        fileLayout = new YAMLFileLayoutBuilder(yamlResourcePath).build();
        root = fileLayout.getRoot();
    }

    @SneakyThrows
    @Override
    public void setupWholeLayout() {
        fileLayout.setupLayout();
    }

    @Override
    public Path getPath() {
        return root.getAbsolutePath();
    }

    @Override
    public Path getLogHomePath() {
        return root.getByKey("logHome").getAbsolutePath();
    }

    @Override
    public Path getProfilerPath() {
        return ((Directory) root.getByKey("judgerWorkspace")).getByKey("profiler").getAbsolutePath();
    }

    @Override
    public SubmissionHome getSubmissionHome(String submissionId) {
        Directory judgerWorkspace = (Directory) root.getByKey("judgerWorkspace");
        Directory submissionHome = (Directory) (judgerWorkspace.getByKey("submissionHome"));
        submissionHome.setName(submissionId);
        return new YAMLSubmissionHome(submissionHome);
    }

    static class YAMLSubmissionHome implements SubmissionHome {
        private Directory submissionHome;

        public YAMLSubmissionHome(Directory submissionHome) {
            this.submissionHome = submissionHome;
        }

        @Override
        public Path getPath() {
            return submissionHome.getAbsolutePath();
        }

        @Override
        public SourceRoot getSourceRoot() {
            return new YAMLSourceRoot((Directory)
                    submissionHome.getByKey("sourceRoot"));
        }

        @Override
        public TestcaseHome getTestCaseHome(String testcaseName) {
            Directory testcaseHome = (Directory) submissionHome.getByKey("testcaseHome");
            testcaseHome.setName(testcaseName);
            return new YAMLTestcaseHome(testcaseHome);
        }
    }

    private static class YAMLSourceRoot implements SourceRoot {
        private Directory sourceRoot;

        YAMLSourceRoot(Directory sourceRoot) {
            this.sourceRoot = sourceRoot;
        }

        @Override
        public Path getCompileScriptPath() {
            return sourceRoot.getByKey("compileScript").getAbsolutePath();
        }

        @Override
        public Path getExecutablePath() {
            return sourceRoot.getByKey("executable").getAbsolutePath();
        }

        @Override
        public Path getPath() {
            return sourceRoot.getAbsolutePath();
        }
    }

    private static class YAMLTestcaseHome implements TestcaseHome {
        private Directory testcaseHome;

        public YAMLTestcaseHome(Directory testcaseHome) {
            this.testcaseHome = testcaseHome;
        }

        @Override
        public Path getPath() {
            return testcaseHome.getAbsolutePath();
        }

        @Override
        public SandboxRoot getSandboxRoot() {
            return new YAMLSandboxRoot(
                    (Directory) testcaseHome.getByKey("sandboxRoot"));
        }

        @Override
        public TestCaseOutputHome getTestcaseOutputHome() {
            return new YAMLTestcaseOutputHome(
                    (Directory) testcaseHome.getByKey("testcaseOutputHome")
            );
        }
    }

    private static class YAMLSandboxRoot implements SandboxRoot {
        private Directory sandboxRoot;

        public YAMLSandboxRoot(Directory sandboxRoot) {
            this.sandboxRoot = sandboxRoot;
        }

        @Override
        public Path getPath() {
            return sandboxRoot.getAbsolutePath();
        }

        @Override
        public Path getStandardInPath() {
            return sandboxRoot.getByKey("stdin").getAbsolutePath();
        }

        @Override
        public Path getActualStandardErrPath() {
            return sandboxRoot.getByKey("actualStderr").getAbsolutePath();
        }

        @Override
        public Path getActualStandardOutPath() {
            return sandboxRoot.getByKey("actualStdout").getAbsolutePath();
        }

        @Override
        public Path getExecutablePath() {
            return sandboxRoot.getByKey("executable").getAbsolutePath();
        }
    }

    private static class YAMLTestcaseOutputHome implements TestCaseOutputHome {
        private Directory testcaseOutputHome;

        public YAMLTestcaseOutputHome(Directory testcaseOutputHome) {
            this.testcaseOutputHome = testcaseOutputHome;
        }

        @Override
        public Path getPath() {
            return testcaseOutputHome.getAbsolutePath();
        }

        @Override
        public Path getExpectedStandardOutPath() {
            return testcaseOutputHome.getByKey("expectedStdout").getAbsolutePath();
        }
    }
}