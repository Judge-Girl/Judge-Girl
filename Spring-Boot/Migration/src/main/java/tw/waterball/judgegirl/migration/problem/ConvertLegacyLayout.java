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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import tw.waterball.judgegirl.commons.helpers.process.SimpleProcessRunner;
import tw.waterball.judgegirl.entities.problem.Compilation;
import tw.waterball.judgegirl.entities.problem.SubmittedCodeSpec;
import tw.waterball.judgegirl.entities.problem.Testcase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.io.FileUtils.copyFile;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static tw.waterball.judgegirl.commons.utils.MyFileUtils.mkdirIfNotExists;
import static tw.waterball.judgegirl.commons.utils.SneakyUtils.sneakyThrow;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@SpringBootApplication
public class ConvertLegacyLayout implements CommandLineRunner {
    private static Logger logger = LogManager.getLogger(ConvertLegacyLayout.class);

    private Input in;
    private ObjectMapper objectMapper;
    private JdbcTemplate jdbcTemplate;
    private ProblemDTO problem = new ProblemDTO();
    private int problemId;
    private Path legacyPackageRootPath;
    private Path outputDirectoryPath;
    private Path testDataHome;
    private Path markdownDescriptionFilePath;
    private List<List> subtasks;
    private Path outputProvidedCodesDirPath;
    private Path outputTestcasesDirPath;

    public static void main(String[] args) {
        SpringApplication.run(ConvertLegacyLayout.class, args);
    }

    public ConvertLegacyLayout(Input in, ObjectMapper objectMapper,
                               JdbcTemplate jdbcTemplate) {
        this.in = in;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        execute();
    }

    // /Users/johnny850807/Documents/Judge-Girl-Migration/package
    // testOutput
    public void execute() throws IOException {
        this.problemId = in.problemId();
        this.problem.id = problemId;
        this.legacyPackageRootPath = in.legacyPackageRootPath();
        this.outputDirectoryPath = in.outputDirectoryPath();
        this.testDataHome = legacyPackageRootPath.resolve("testData")
                .resolve(String.valueOf(problemId));
        this.markdownDescriptionFilePath = legacyPackageRootPath
                .resolve("source").resolve("md").resolve("problem")
                .resolve(format("%d.md", problemId));

        setupOutputLayout();
        queryProblemFromDB();
        copyFile(markdownDescriptionFilePath.toFile(),
                outputDirectoryPath.resolve("description.md").toFile());
        subtasks = pythonSubtasksToJsonString();
        addSubtasksAsTestcasesIntoProblem();
        makeTestcaseIOsLayout();
        outputProblemAsJson();
    }

    private void queryProblemFromDB() {
        jdbcTemplate.query("select * from problems where pid=?",
                new Object[]{problemId},
                resultSet -> {
                    problem.title = resultSet.getString("ttl");
                });
    }


    /**
     * @return a list structure as below
     * [
     *    [  0, ['0.in', '0.out', 1, 64 << 20, 64 << 10]]
     *    ...
     * ]
     */
    @SuppressWarnings("unchecked")
    private List<List> pythonSubtasksToJsonString() throws IOException {
        File subtasksPythonFile = testDataHome.resolve("subtasks.py").toFile();
        String subtasksPythonRawString = IOUtils.toString(new FileInputStream(subtasksPythonFile), StandardCharsets.UTF_8);
        subtasksPythonRawString = "import json\n" +
                "print(json.dumps(\n" + subtasksPythonRawString +
                "\n))";
        IOUtils.write(subtasksPythonRawString.getBytes(),
                new FileOutputStream(testDataHome.resolve("subtasks.json.py").toFile()));
        SimpleProcessRunner processRunner = new SimpleProcessRunner();
        processRunner.execute(testDataHome, "python", "subtasks.json.py");
        processRunner.awaitTermination();
        String subtasksJson = processRunner.getStdout();
        logger.info(subtasksJson);
        return (List<List>) objectMapper.readValue(subtasksJson, Object.class);
    }

    private void addSubtasksAsTestcasesIntoProblem() {
        for (List subtask : subtasks) {
            Testcase testcase = new Testcase();
            testcase.setGrade((int) subtask.get(0));
            List caseAttr = (List) subtask.get(1);
            String stdinFileName = (String) caseAttr.get(0);
            String stdoutFileName = (String) caseAttr.get(1);
            assert getBaseName(stdinFileName).equals(getBaseName(stdoutFileName))
                    : "The testcase name cannot be derived since " +
                    "the stdin's and stdout's name are different:" + stdinFileName + "," + stdoutFileName;
            testcase.setName(getBaseName(stdinFileName));
            testcase.setProblemId(problemId);
            testcase.setTimeLimit((Integer) caseAttr.get(2));
            testcase.setMemoryLimit(((Integer) caseAttr.get(3)).longValue());
            testcase.setOutputLimit(((Integer) caseAttr.get(4)).longValue());
            testcase.setThreadNumberLimit(-1);
            problem.testcases.add(testcase);
        }
    }

    private void setupOutputLayout() {
        outputProvidedCodesDirPath = outputDirectoryPath.resolve("providedCodes");
        outputTestcasesDirPath = outputDirectoryPath.resolve("testcases");
        assert mkdirIfNotExists(outputProvidedCodesDirPath);
        assert mkdirIfNotExists(outputTestcasesDirPath);
    }

    private void makeTestcaseIOsLayout() {
        List<IoFilePair> stdIoFilePairs = getStdIoFilePairs();

        stdIoFilePairs.forEach(stdIoFilePair -> {
                    Path testcaseDirPath = outputTestcasesDirPath.resolve(getBaseName(stdIoFilePair.name));
                    assert mkdirIfNotExists(testcaseDirPath);
                    Path inPath = testcaseDirPath.resolve("in");
                    Path outPath = testcaseDirPath.resolve("out");
                    assert mkdirIfNotExists(inPath);
                    assert mkdirIfNotExists(outPath);
                    sneakyThrow(() -> {
                        copyFile(stdIoFilePair.in, inPath.resolve("std.in").toFile());
                        copyFile(stdIoFilePair.out, inPath.resolve("std.out").toFile());
                    });
                }
        );
    }

    private List<IoFilePair> getStdIoFilePairs() {
        Map<String, List<File>> stdIoPairByName = Arrays.stream(requireNonNull(testDataHome.toFile().listFiles()))
                .filter(file -> "in".equals(getExtension(file.getName())) ||
                        "out".equals(getExtension(file.getName())))
                .collect(groupingBy(f -> getBaseName(f.getName())));
        return stdIoPairByName.entrySet().stream()
                .map(en -> {
                    assert en.getValue().size() == 2 : "There is a IO pair where it only has either the input or the output.";
                    // make sure that the 'in' is at the first element
                    en.getValue().sort((f1, f2) -> "in".equals(getExtension(f1.getName())) ? -1 : 1);
                    return new IoFilePair(en.getKey(),
                            en.getValue().get(0), en.getValue().get(1));
                })
                .collect(Collectors.toList());
    }

    private void outputProblemAsJson() throws IOException {
        String problemJson = objectMapper
                .writerWithDefaultPrettyPrinter().writeValueAsString(problem);
        IOUtils.write(problemJson, new FileOutputStream(
                outputDirectoryPath.resolve("problem.json").toFile()), StandardCharsets.UTF_8);
        logger.info(problemJson);
    }

    public class ProblemDTO {
        public int id;
        public String title;
        public List<SubmittedCodeSpec> submittedCodeSpecs = new ArrayList<>();
        public List<String> tags = new ArrayList<>();
        public Compilation compilation = new Compilation();
        public List<String> inputFileNames = new ArrayList<>();
        public List<String> outputFileNames = new ArrayList<>();
        public List<Testcase> testcases = new ArrayList<>();
    }

    public interface Input {
        int problemId();

        Path legacyPackageRootPath();

        Path outputDirectoryPath();
    }

    @AllArgsConstructor
    public class IoFilePair {
        public String name;
        public File in;
        public File out;
    }

}
