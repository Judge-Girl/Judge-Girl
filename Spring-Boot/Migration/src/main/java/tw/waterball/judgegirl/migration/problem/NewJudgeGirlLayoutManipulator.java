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

package tw.waterball.judgegirl.migration.problem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.utils.DirectoryUtils;
import tw.waterball.judgegirl.commons.utils.ToStringUtils;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.problem.Testcase;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static tw.waterball.judgegirl.commons.utils.functional.FunctionalUtils.noneMatch;
import static tw.waterball.judgegirl.migration.utils.FileAssertions.assertDirectoryExists;
import static tw.waterball.judgegirl.migration.utils.FileAssertions.assertFileExists;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Component
public class NewJudgeGirlLayoutManipulator {
    private static final Logger logger = LogManager.getLogger();
    public static final String TESTCASES_HOME = "testcases";
    public static final String PROVIDED_CODES_DIR = "providedCodes";
    public static final String IMAGES_DIR = "images";
    public static final String PROBLEM_JSON = "problem.json";
    public static final String DESCRIPTION_MD = "description.md";

    private ObjectMapper objectMapper;

    public NewJudgeGirlLayoutManipulator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void verifyProblemDirLayout(Path problemDirPath) {
        assertDirectoryExists(problemDirPath.resolve(IMAGES_DIR));
        assertDirectoryExists(problemDirPath.resolve(TESTCASES_HOME));
        assertDirectoryExists(problemDirPath.resolve(PROVIDED_CODES_DIR));
        assertFileExists(problemDirPath.resolve(DESCRIPTION_MD));
        assertFileExists(problemDirPath.resolve(PROBLEM_JSON));
    }

    @SneakyThrows
    public Problem verifyAndReadProblem(Path problemDirPath) {
        JsonNode jsonNode = objectMapper.readTree(new FileReader(problemDirPath.resolve(PROBLEM_JSON).toFile()));
        Problem problem = objectMapper.treeToValue(jsonNode, Problem.class);
        verifyProblemReadFromJson(problem);
        problem.setMarkdownDescription(IOUtils.toString(new FileReader(
                problemDirPath.resolve(DESCRIPTION_MD).toFile())));
        logger.info(problem);
        return problem;
    }

    @SneakyThrows
    public List<Testcase> verifyAndReadTestcases(Path problemDirPath) {
        JsonNode jsonNode = objectMapper.readTree(new FileReader(problemDirPath.resolve(PROBLEM_JSON).toFile()));
        Problem problem = objectMapper.treeToValue(jsonNode, Problem.class);
        if (!jsonNode.has("testcases")) {
            throw new IllegalStateException("The " + PROBLEM_JSON + " should have `testcases` attribute.");
        }
        List<Testcase> testcases = objectMapper.readValue(
                objectMapper.treeAsTokens(jsonNode.get("testcases")), new TypeReference<List<Testcase>>() {});
        verifyTestcasesReadFromJson(testcases);
        verifyTestcaseHomeLayout(problemDirPath, problem, testcases);
        logger.info(ToStringUtils.toString(testcases, "\n"));
        return testcases;

    }

    private void verifyProblemReadFromJson(Problem problem) {
        requireNonNull(problem.getTitle());
        requireNonNull(problem.getSubmittedCodeSpecs());
        if (problem.getSubmittedCodeSpecs().size() < 1) {
            throw new IllegalStateException("The problem has no submittedCodeSpecs, why?");
        }
        requireNonNull(problem.getTags());
        requireNonNull(problem.getCompilation());
        requireNonNull(problem.getInputFileNames());
        requireNonNull(problem.getOutputFileNames());
    }

    private void verifyTestcasesReadFromJson(List<Testcase> testcases) {
        if (testcases.size() < 1) {
            throw new IllegalStateException("The problem has no testcases, why?");
        }

        for (Testcase testcase : testcases) {
            requireNonNull(testcase.getName());
            if (testcase.getGrade() < 0) {
                throw new IllegalStateException("Testcase with negative grade found.");
            } else if (testcase.getMemoryLimit() == 0) {
                throw new IllegalStateException("Testcase with zero memory limit found. Set -1 to indicate `non-limited`.");
            } else if (testcase.getOutputLimit() == 0) {
                throw new IllegalStateException("Testcase with zero output limit found. Set -1 to indicate `non-limited`.");
            } else if (testcase.getTimeLimit() == 0) {
                throw new IllegalStateException("Testcase with zero time limit found. Set -1 to indicate `non-limited`.");
            } else if (testcase.getThreadNumberLimit() == 0) {
                throw new IllegalStateException("Testcase with zero thread number limit found. Set -1 to indicate `non-limited`.");
            }
        }
    }

    private void verifyTestcaseHomeLayout(Path problemDirPath, Problem problem, List<Testcase> testcases) {
        File[] testcaseDirs = problemDirPath.resolve(TESTCASES_HOME).toFile().listFiles();
        assert testcaseDirs != null : problemDirPath.resolve(TESTCASES_HOME) + " must be a directory.";
        testcaseDirs = DirectoryUtils.removeHiddenDirectories(testcaseDirs);

        if (testcases.size() != testcaseDirs.length) {
            throw new IllegalStateException("The number of testcase's directories is not equal to the number of the testcases read from " + PROBLEM_JSON);
        }

        Arrays.stream(testcaseDirs)
                .forEach(testcaseDir -> verifyTestcaseDirLayout(testcaseDir, problem, testcases));
    }


    private void verifyTestcaseDirLayout(File testcaseDir, Problem problem, List<Testcase> testcases) {
        if (noneMatch(testcases, Testcase::getName, testcaseDir.getName())) {
            throw new IllegalArgumentException("The testcase directory's name " + testcaseDir.getName() + "does not match to any testcase's name.");
        }
        Path inputDirPath = testcaseDir.toPath().resolve("in");
        Path outputDirPath = testcaseDir.toPath().resolve("out");
        assertDirectoryExists(inputDirPath);
        assertDirectoryExists(outputDirPath);
        assertFileExists(inputDirPath.resolve("std.in"));
        assertFileExists(outputDirPath.resolve("std.out"));
        for (String inputFileName : problem.getInputFileNames()) {
            assertFileExists(inputDirPath.resolve(inputFileName));
        }
        for (String outputFileName : problem.getOutputFileNames()) {
            assertFileExists(outputDirPath.resolve(outputFileName));
        }
    }
}
