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

package tw.waterball.judgegirl.migration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import tw.waterball.judgegirl.commons.utils.ArrayUtils;
import tw.waterball.judgegirl.commons.utils.ZipUtils;
import tw.waterball.judgegirl.commons.utils.functional.ErrConsumer;
import tw.waterball.judgegirl.entities.problem.*;
import tw.waterball.judgegirl.plugins.api.match.JudgeGirlMatchPolicyPlugin;
import tw.waterball.judgegirl.plugins.impl.match.AllMatchPolicyPlugin;
import tw.waterball.judgegirl.plugins.impl.match.RegexMatchPolicyPlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static tw.waterball.judgegirl.migration.Inputs.*;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@SpringBootApplication
public class MigrateOneProblem implements CommandLineRunner {

    private static final String TESTCASES_HOME = "testcases";
    private static final String PROVIDED_CODES_DIR = "providedCodes";
    private static final String IMAGES_DIR = "images";
    private static final String PROBLEM_JSON = "problem.json";
    private static final String DESCRIPTION_MD = "description.md";
    private Problem problem;
    private List<Testcase> testcases;
    private Scanner scanner;
    private MongoTemplate mongoTemplate;
    private GridFsTemplate gridFsTemplate;
    private ObjectMapper objectMapper;
    private JudgeGirlMatchPolicyPlugin[] matchPolicyPlugins =
            {new AllMatchPolicyPlugin(), new RegexMatchPolicyPlugin()};

    public static void main(String[] args) {
        SpringApplication.run(MigrateOneProblem.class, args);
    }

    public MigrateOneProblem(MongoTemplate mongoTemplate,
                             GridFsTemplate gridFsTemplate, ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.gridFsTemplate = gridFsTemplate;
        this.objectMapper = objectMapper;
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void run(String... args) throws Exception {
        scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Input the problem directory path: ");
            String problemDir = "";
            do {
                try {
                    problemDir = scanner.nextLine();

                    if (!problemDir.isEmpty()) {
                        Path problemDirPath = Paths.get(problemDir);
                        verifyProblemDirLayout(problemDirPath);

                        readProblemFromFiles(problemDirPath, objectMapper);
                        populateProblemAndTestcasesWithNewSchema();
                        migrateToJudgeGirlDatabase(problemDirPath);
                    }
                } catch (InvalidPathException err) {
                    System.err.println(err.getMessage());
                }

            } while (problemDir.isEmpty());
        }

    }

    private void verifyProblemDirLayout(Path problemDirPath) {
        assertFileExists(problemDirPath.resolve(IMAGES_DIR), true);
        assertFileExists(problemDirPath.resolve(TESTCASES_HOME), true);
        assertFileExists(problemDirPath.resolve(PROVIDED_CODES_DIR), true);
        assertFileExists(problemDirPath.resolve(DESCRIPTION_MD), false);
        assertFileExists(problemDirPath.resolve(PROBLEM_JSON), false);
    }

    private void assertFileExists(Path filePath, boolean assertDirectory) {
        if (!Files.exists(filePath)) {
            throw new IllegalStateException(String.format("The path %s must exist. \n", filePath));
        } else if (assertDirectory && !Files.isDirectory(filePath)) {
            throw new IllegalStateException(String.format("%s must be a directory. \n", filePath));
        }
    }

    private void readProblemFromFiles(Path problemDirPath, ObjectMapper objectMapper) {
        try {
            JsonNode jsonNode = objectMapper.readTree(new FileReader(
                    problemDirPath.resolve(PROBLEM_JSON).toFile()));

            problem = objectMapper.treeToValue(jsonNode, Problem.class);
            verifyProblemReadFromJson(problem);

            if (!jsonNode.has("testcases")) {
                throw new IllegalStateException("The " + PROBLEM_JSON + " should have `testcases` attribute.");
            }

            testcases = objectMapper.readValue(
                    objectMapper.treeAsTokens(jsonNode.get("testcases")), new TypeReference<List<Testcase>>() {});
            verifyTestcasesReadFromJson(testcases);

            verifyTestcaseHomeLayout(problemDirPath);

            problem.setMarkdownDescription(IOUtils.toString(new FileReader(
                    problemDirPath.resolve(DESCRIPTION_MD).toFile())));

            System.out.println(problem);
            System.out.println(testcases.stream().map(Object::toString).collect(Collectors.joining("\n")));

        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException("Error within problem.json.", e);
        }
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

    private void verifyTestcaseHomeLayout(Path problemDirPath) {
        File[] testcaseDirs = problemDirPath.resolve(TESTCASES_HOME).toFile().listFiles();
        assert testcaseDirs != null : problemDirPath.resolve(TESTCASES_HOME) + " must be a directory.";
        testcaseDirs = Arrays.stream(testcaseDirs)
                .filter(d -> !d.isHidden())
                .toArray(File[]::new);

        if (testcases.size() != testcaseDirs.length) {
            throw new IllegalStateException("The number of testcase's directories is not equal to the number of the testcases read from " + PROBLEM_JSON);
        }

        Arrays.stream(testcaseDirs)
                .forEach(this::verifyTestcaseDirLayout);
    }

    private void verifyTestcaseDirLayout(File testcaseDir) {
        if (testcases.stream()
                .noneMatch(t -> t.getName().equals(testcaseDir.getName()))) {
            throw new IllegalArgumentException("The testcase directory's name " + testcaseDir.getName() +
                    "does not match to any testcase's name.");
        }
        Path inputDirPath = testcaseDir.toPath().resolve("in");
        Path outputDirPath = testcaseDir.toPath().resolve("out");
        assertFileExists(inputDirPath, true);
        assertFileExists(outputDirPath, true);
        assertFileExists(inputDirPath.resolve("std.in"), false);
        assertFileExists(outputDirPath.resolve("std.out"), false);
        for (String inputFileName : problem.getInputFileNames()) {
            assertFileExists(inputDirPath.resolve(inputFileName), false);
        }
        for (String outputFileName : problem.getOutputFileNames()) {
            assertFileExists(outputDirPath.resolve(outputFileName), false);
        }
    }

    private void populateProblemAndTestcasesWithNewSchema() {
        problem.setJudgeSpec(new JudgeSpec(Language.C, JudgeEnv.NORMAL,
                inputRangedNumberOrDefault("Input allocated CPU ", 2f, 0.1f, 100),
                inputRangedNumberOrDefault("Input allocated GPU ", 1f, 0, 10)));

        for (int i = 0; i < matchPolicyPlugins.length; i++) {
            System.out.printf("[%d] -- %s%n", i, matchPolicyPlugins[i].getTag());
        }
        int selection = inputRangedIntegerOrDefault("Please select the plugin: ", 0, 0, matchPolicyPlugins.length-1);
        problem.setOutputMatchPolicyPluginTag(matchPolicyPlugins[selection].getTag());
    }

    private void migrateToJudgeGirlDatabase(Path problemDirPath) {
        System.out.println("Migrating ...");

        List<Problem> problems = mongoTemplate.findAll(Problem.class);

        Optional<Problem> replacedProblemOptional = askForReplacingProblem(problems);
        if (replacedProblemOptional.isPresent()) {
            cleanProblem(replacedProblemOptional.get());
            problems.remove(replacedProblemOptional.get());
            problem.setId(replacedProblemOptional.get().getId());
        } else {
            int problemId = askForSpecifyingProblemId(problems)
                    .orElseGet(() -> /* get the next maximum id by query*/
                            problems.stream().max(Comparator.comparingInt(Problem::getId))
                                    .map(Problem::getId).orElse(0) + 1);
            problem.setId(problemId);
        }

        try {
            saveProvidedCodes(problemDirPath);
            saveTestcaseInputAndOutputs(problemDirPath);
        } catch (Exception err) {
            err.printStackTrace();
        }

        // save problem only after so the fileIds are updated
        saveProblem();
        saveTestcases();

        System.out.println("Migration completed.");
    }

    private Optional<Problem> askForReplacingProblem(List<Problem> existingProblems) {
        if (inputForYesOrNo("Would you like to replace the existing problem?")) {
            String options = existingProblems.stream().map(p -> String.format("[%d] %s", p.getId(), p.getTitle()))
                    .collect(Collectors.joining("\n"));
            List<Integer> idList = existingProblems.stream().map(Problem::getId).collect(Collectors.toList());
            options += "\nPlease select a problem id";
            int input = Inputs.inputRangedInteger(options, idList);
            return existingProblems.stream().filter(p -> p.getId() == input).findFirst();
        } else {
            return Optional.empty();
        }
    }

    private void cleanProblem(Problem problem) {
        mongoTemplate.remove(problem);
        mongoTemplate.remove(query(where("problemId").is(problem.getId())), Testcase.class);
        gridFsTemplate.delete(query(where("_id").is(problem.getProvidedCodesFileId())));
        gridFsTemplate.delete(query(where("_id").is(problem.getTestcaseIOsFileId())));
    }

    private OptionalInt askForSpecifyingProblemId(List<Problem> existingProblems) {
        if (inputForYesOrNo("Would you like to specify the problem's id")) {
            int problemId = Inputs.inputConditionalInteger("Input the problem's id",
                    id -> id >= 0 && existingProblems.stream().noneMatch(p -> p.getId().equals(id)),
                    id -> String.format("The input id %d must not exist and be positive.", id));
            return OptionalInt.of(problemId);
        } else {
            return OptionalInt.empty();
        }
    }

    private void saveProvidedCodes(Path problemDirPath) throws Exception {
        System.out.println("Saving providedCodes ...");
        File[] providedCodes = problemDirPath.resolve(PROVIDED_CODES_DIR).toFile().listFiles();
        assert providedCodes != null;
        zip(providedCodes, zip -> {
            String fileId = gridFsTemplate.store(new FileInputStream(zip), problem.getProvidedCodesFileName()).toString();
            System.out.println("Successfully store zipped provided codes in mongodb, fileId: " + fileId);
            problem.setProvidedCodesFileId(fileId);
        });
        System.out.println("Saved providedCodes.");
    }


    private void saveTestcaseInputAndOutputs(Path problemDirPath) throws Exception {
        System.out.println("Saving providedCodes ...");
        File[] testcaseDirs = problemDirPath.resolve(TESTCASES_HOME).toFile().listFiles();
        assert testcaseDirs != null;

        zip(testcaseDirs, zip -> {
            String fileId = gridFsTemplate.store(new FileInputStream(zip), problem.getTestCaseIOsFileName()).toString();
            System.out.println("Successfully store zipped testcase inputs in mongodb, fileId: " + fileId);
            problem.setTestcaseIOsFileId(fileId);
        });

        System.out.println("Saved providedCodes ...");
    }

    private void zip(File[] files, ErrConsumer<File> zipFileConsumer, String... ignoredFileNames) throws Exception {
        System.out.println("Zipping ... \n" +
                Arrays.stream(files)
                        .filter(f -> !ArrayUtils.contains(ignoredFileNames, f.getName()))
                        .map(File::getPath).collect(Collectors.joining("\n")));

        File zipOutputFile = File.createTempFile("judge-girl-", "");
        ZipUtils.zipToFile(files, new FileOutputStream(zipOutputFile), ignoredFileNames);
        System.out.println("Successfully zipped in " + zipOutputFile.getPath());
        zipFileConsumer.accept(zipOutputFile);
        zipOutputFile.delete();
        System.out.println("Deleted temp file " + zipOutputFile.getPath());
    }

    private void saveProblem() {
        System.out.println("Saving problem ...");
        problem = mongoTemplate.save(problem);
        assert problem.equals(mongoTemplate.findById(problem.getId(), Problem.class));
        System.out.println("Saved problem, " + problem);
    }

    private void saveTestcases() {
        System.out.println("Saving testcases ...");
        for (Testcase testcase : testcases) {
            testcase.setProblemId(problem.getId());
            Testcase saved = mongoTemplate.save(testcase);
            System.out.println("Saved testcase: " + saved);
        }
        System.out.println("Saved testcases ...");
    }


}
