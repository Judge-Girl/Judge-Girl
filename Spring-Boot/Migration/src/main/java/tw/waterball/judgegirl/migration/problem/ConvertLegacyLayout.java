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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import tw.waterball.judgegirl.commons.helpers.process.SimpleProcessRunner;
import tw.waterball.judgegirl.entities.problem.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
public class ConvertLegacyLayout {
    public static final Language DEFAULT_LANGUAGE = Language.C;
    private static final Logger logger = LogManager.getLogger(ConvertLegacyLayout.class);
    public static final String DEFAULT_SUBMITTED_CODE_FILE_NAME = "a.c";

    private final Input in;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private int problemId;
    private final Problem problem = new Problem();
    private Path outputDirectoryPath;
    private Path testDataHome;
    private List<List> subtasks;
    private Path outputTestcasesDirPath;
    private Path outputProvidedCodesDirPath;

    public static void main(String[] args) {
        var context = SpringApplication.run(ConvertLegacyLayout.class, args);
        try {
            ConvertLegacyLayout convertLegacyLayout = context.getBean(ConvertLegacyLayout.class);
            convertLegacyLayout.execute();
            System.out.println("Completed");
            SpringApplication.exit(context, () -> 1);
        } catch (Exception err) {
            err.printStackTrace();
            SpringApplication.exit(context, () -> -1);
        }
    }

    public ConvertLegacyLayout(Input in, ObjectMapper objectMapper,
                               JdbcTemplate jdbcTemplate) {
        this.in = in;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    // testOutput
    public void execute() throws IOException {
        this.problemId = in.problemId();
        this.problem.setId(problemId);
        problem.addLanguageEnv(new LanguageEnv(DEFAULT_LANGUAGE));
        Path legacyPackageRootPath = in.legacyPackageRootPath();
        this.outputDirectoryPath = in.outputDirectoryPath();
        this.testDataHome = legacyPackageRootPath.resolve("testdata")
                .resolve(String.valueOf(problemId));
        Path markdownDescriptionFilePath = legacyPackageRootPath
                .resolve("source").resolve("md").resolve("problem")
                .resolve(format("%d.md", problemId));

        setupOutputLayout();
        queryProblemFromDB();
        copyFile(markdownDescriptionFilePath.toFile(),
                outputDirectoryPath.resolve("description.md").toFile());
        subtasks = pythonSubtasksToJsonString();
        addSubtasksAsTestcasesIntoProblem();
        makeTestcaseIOsLayout();
        addSubmittedCodeSpecs();
        migrateProvidedCodes();
        problem.getLanguageEnv(DEFAULT_LANGUAGE).setCompilationScript(in.compilationScript());
        problem.setTags(Arrays.asList(in.tags()));
        outputProblemAsJson();
    }

    private void queryProblemFromDB() {
        jdbcTemplate.query("select * from problems where pid=?",
                new Object[]{problemId},
                resultSet -> {
                    problem.setTitle(resultSet.getString("ttl"));
                });
    }


    /**
     * @return a list structure as below
     * [
     * [  0, ['0.in', '0.out', 1, 64 << 20, 64 << 10]]
     * ...
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
            if (!(caseAttr.get(1) instanceof String)) {
                throw new IllegalStateException("Unexpected subtask's format.");
            }
            String stdoutFileName = (String) caseAttr.get(1);
            assert getBaseName(stdinFileName).equals(getBaseName(stdoutFileName))
                    : "The testcase name cannot be derived since " +
                    "the stdin's and stdout's name are different:" + stdinFileName + "," + stdoutFileName;
            testcase.setName(getBaseName(stdinFileName));
            testcase.setProblemId(problemId);
            testcase.setTimeLimit((Integer) caseAttr.get(2) * 1000);
            testcase.setMemoryLimit(((Integer) caseAttr.get(3)).longValue());
            testcase.setOutputLimit(((Integer) caseAttr.get(4)).longValue());
            testcase.setThreadNumberLimit(-1);
            problem.getTestcases().add(testcase);
        }
    }

    private void setupOutputLayout() throws IOException {
        if (Files.exists(outputDirectoryPath)) {
            FileUtils.deleteDirectory(outputDirectoryPath.toFile());
        }
        outputProvidedCodesDirPath = outputDirectoryPath.resolve("providedCodes");
        outputTestcasesDirPath = outputDirectoryPath.resolve("testcases");
        assert mkdirIfNotExists(outputDirectoryPath);
        assert mkdirIfNotExists(outputProvidedCodesDirPath);
        assert mkdirIfNotExists(outputDirectoryPath.resolve("images"));
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
                        copyFile(stdIoFilePair.out, outPath.resolve("std.out").toFile());
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

    private void addSubmittedCodeSpecs() throws IOException {
        if (Files.exists(testDataHome.resolve("source.lst"))) {
            Files.readAllLines(testDataHome.resolve("source.lst"))
                    .forEach(submittedCodeFileName ->
                            problem.getLanguageEnv(DEFAULT_LANGUAGE).getSubmittedCodeSpecs().add(new SubmittedCodeSpec(Language.C, submittedCodeFileName)));
        } else {
            problem.getLanguageEnv(DEFAULT_LANGUAGE).getSubmittedCodeSpecs().add(new SubmittedCodeSpec(Language.C, DEFAULT_SUBMITTED_CODE_FILE_NAME));
        }

    }

    private void migrateProvidedCodes() throws IOException {
        if (Files.exists(testDataHome.resolve("send.lst"))) {
            String sendList = Files.readString(testDataHome.resolve("send.lst"));
            Arrays.stream(sendList.split("\n"))
                    .map(String::trim)
                    .forEach(providedCodeName -> {
                        try {
                            FileUtils.copyFile(testDataHome.resolve(providedCodeName).toFile(),
                                    outputProvidedCodesDirPath.resolve(providedCodeName).toFile());
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    });
        }
    }

    private void outputProblemAsJson() throws IOException {
        String problemJson = objectMapper
                .writerWithDefaultPrettyPrinter().writeValueAsString(problem);
        IOUtils.write(problemJson, new FileOutputStream(
                outputDirectoryPath.resolve("problem.json").toFile()), StandardCharsets.UTF_8);
        logger.info(problemJson);
    }


    public interface Input {
        int problemId();

        Path legacyPackageRootPath();

        Path outputDirectoryPath();

        String compilationScript();

        String[] tags();
    }

    @AllArgsConstructor
    public static class IoFilePair {
        public String name;
        public File in;
        public File out;
    }

}
