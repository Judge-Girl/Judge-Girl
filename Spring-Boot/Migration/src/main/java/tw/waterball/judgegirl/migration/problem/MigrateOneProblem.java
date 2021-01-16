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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import tw.waterball.judgegirl.commons.utils.ArrayUtils;
import tw.waterball.judgegirl.commons.utils.FlowUtils;
import tw.waterball.judgegirl.commons.utils.ZipUtils;
import tw.waterball.judgegirl.commons.utils.functional.ErrConsumer;
import tw.waterball.judgegirl.entities.problem.Language;
import tw.waterball.judgegirl.entities.problem.LanguageEnv;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.problem.Testcase;
import tw.waterball.judgegirl.migration.problem.in.InputStrategy;
import tw.waterball.judgegirl.plugins.api.match.JudgeGirlMatchPolicyPlugin;
import tw.waterball.judgegirl.plugins.impl.match.AllMatchPolicyPlugin;
import tw.waterball.judgegirl.plugins.impl.match.RegexMatchPolicyPlugin;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static tw.waterball.judgegirl.migration.problem.NewJudgeGirlLayoutManipulator.PROVIDED_CODES_DIR;
import static tw.waterball.judgegirl.migration.problem.NewJudgeGirlLayoutManipulator.TESTCASES_HOME;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@SuppressWarnings("SameParameterValue")
@SpringBootApplication
public class MigrateOneProblem implements CommandLineRunner {
    public static final Language DEFAULT_LANGUAGE = Language.C;
    public static final String PROVIDED_CODES_FILE_NAME_PATTERN = "%d-%s-provided.zip";
    public static final String TESTCASES_IO_FILE_NAME_PATTERN = "%d-testcases.zip";
    private static final Logger logger = LogManager.getLogger();

    private final InputStrategy in;
    private final MongoTemplate mongoTemplate;
    private final GridFsTemplate gridFsTemplate;
    private final NewJudgeGirlLayoutManipulator layoutManipulator;
    private final JudgeGirlMatchPolicyPlugin[] matchPolicyPlugins =
            {new AllMatchPolicyPlugin(), new RegexMatchPolicyPlugin()};

    private Problem problem;
    private List<Testcase> testcases;

    public static void main(String[] args) {
        SpringApplication.run(MigrateOneProblem.class, args);
    }

    @PostConstruct
    public void objectMapperConfig(ObjectMapper objectMapper) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public MigrateOneProblem(InputStrategy in, MongoTemplate mongoTemplate,
                             GridFsTemplate gridFsTemplate, NewJudgeGirlLayoutManipulator layoutManipulator) {
        this.in = in;
        this.mongoTemplate = mongoTemplate;
        this.gridFsTemplate = gridFsTemplate;
        this.layoutManipulator = layoutManipulator;
    }

    @Override
    public void run(String... args) {
        this.execute(1);
    }

    private void execute(int repeatOnFailureTime) {
        FlowUtils.repeatUntil(() -> {
            Path problemDirPath = Paths.get(in.problemDirPath());
            layoutManipulator.verifyProblemDirLayout(problemDirPath);
            problem = layoutManipulator.verifyAndReadProblem(problemDirPath);
            testcases = layoutManipulator.verifyAndReadTestcases(problemDirPath);
            populateProblemAndTestcasesWithNewSchema();
            migrateToJudgeGirlDatabase(problemDirPath);
        }, repeatOnFailureTime);
    }

    private void populateProblemAndTestcasesWithNewSchema() {
        LanguageEnv langEnv = problem.getLanguageEnv(DEFAULT_LANGUAGE);
        langEnv.setResourceSpec(in.resourceSpec());
        problem.setOutputMatchPolicyPluginTag(
                in.matchPolicyPlugin(matchPolicyPlugins).getTag());
    }

    private void migrateToJudgeGirlDatabase(Path problemDirPath) {
        logger.info("Migrating ...");

        List<Problem> problems = mongoTemplate.findAll(Problem.class);
        replaceExistingProblemIfAgree(problems);

        try {
            saveProvidedCodes(problemDirPath);
            saveTestcaseInputAndOutputs(problemDirPath);
        } catch (Exception err) {
            err.printStackTrace();
        }

        // save problem only after so the fileIds have been updated
        saveProblem();
        saveTestcases();

        logger.info("Migration completed.");
    }

    private void replaceExistingProblemIfAgree(List<Problem> problems) {
        in.replaceExistingProblemOrNot(problems)
                .ifPresentOrElse(p -> replaceExistingProblem(problems, p),
                        () -> specifyProblemIdIfAgreeOtherwiseUseIncrementalId(problems));
    }

    private void replaceExistingProblem(List<Problem> problems, Problem newProblem) {
        deleteProblemInDB(newProblem);
        problems.remove(newProblem);
        problem.setId(newProblem.getId());
    }

    private void deleteProblemInDB(Problem problem) {
        LanguageEnv langEnv = problem.getLanguageEnv(DEFAULT_LANGUAGE);
        mongoTemplate.remove(problem);
        mongoTemplate.remove(query(where("problemId").is(problem.getId())), Testcase.class);
        gridFsTemplate.delete(query(where("_id").is(langEnv.getProvidedCodesFileId())));
        gridFsTemplate.delete(query(where("_id").is(problem.getTestcaseIOsFileId())));
    }

    private void specifyProblemIdIfAgreeOtherwiseUseIncrementalId(List<Problem> problems) {
        int problemId = in.specifyProblemIdOrNot(problems)
                .orElseGet(() -> /* get the next maximum id by query*/
                        problems.stream().max(Comparator.comparingInt(Problem::getId))
                                .map(Problem::getId).orElse(0) + 1);
        problem.setId(problemId);
    }

    private void saveProvidedCodes(Path problemDirPath) throws Exception {
        logger.info("Saving providedCodes ...");
        LanguageEnv langEnv = problem.getLanguageEnv(DEFAULT_LANGUAGE);
        File[] providedCodes = problemDirPath.resolve(PROVIDED_CODES_DIR).toFile().listFiles();
        assert providedCodes != null;
        zip(providedCodes, zip -> {
            String providedCodesFileName = String.format(PROVIDED_CODES_FILE_NAME_PATTERN, problem.getId(), langEnv.getName());
            String fileId = gridFsTemplate.store(new FileInputStream(zip), providedCodesFileName).toString();
            logger.info("Successfully store zipped provided codes in mongodb, fileId: " + fileId);
            langEnv.setProvidedCodesFileId(fileId);
        });
        logger.info("Saved providedCodes.");
    }


    private void saveTestcaseInputAndOutputs(Path problemDirPath) throws Exception {
        logger.info("Saving providedCodes ...");
        File[] testcaseDirs = problemDirPath.resolve(TESTCASES_HOME).toFile().listFiles();
        assert testcaseDirs != null;

        zip(testcaseDirs, zip -> {
            String testcaseIoFileName = String.format(TESTCASES_IO_FILE_NAME_PATTERN, problem.getId());
            String fileId = gridFsTemplate.store(new FileInputStream(zip), testcaseIoFileName).toString();
            logger.info("Successfully store zipped testcase inputs in mongodb, fileId: " + fileId);
            problem.setTestcaseIOsFileId(fileId);
        });

        logger.info("Saved providedCodes ...");
    }

    private void zip(File[] files, ErrConsumer<File> zipFileConsumer, String... ignoredFileNames) throws Exception {
        logZippedFiles(files, ignoredFileNames);

        File zipOutputFile = File.createTempFile("judge-girl-", "");
        ZipUtils.zipToFile(files, new FileOutputStream(zipOutputFile), ignoredFileNames);
        logger.info("Successfully zipped in " + zipOutputFile.getPath());
        zipFileConsumer.accept(zipOutputFile);
        zipOutputFile.delete();
        logger.info("Deleted temp file " + zipOutputFile.getPath());
    }

    private void logZippedFiles(File[] files, String[] ignoredFileNames) {
        logger.info("Zipping ... \n" +
                Arrays.stream(files)
                        .filter(f -> !ArrayUtils.contains(ignoredFileNames, f.getName()))
                        .map(File::getPath).collect(Collectors.joining("\n")));
    }

    private void saveProblem() {
        logger.info("Saving problem ...");
        requireNonNull(problem.getLanguageEnv(DEFAULT_LANGUAGE).getProvidedCodesFileId());
        requireNonNull(problem.getTestcaseIOsFileId());
        problem = mongoTemplate.save(problem);
        assert problem.equals(mongoTemplate.findById(problem.getId(), Problem.class));
        logger.info("Saved problem, " + problem);
    }

    private void saveTestcases() {
        logger.info("Saving testcases ...");
        for (Testcase testcase : testcases) {
            testcase.setProblemId(problem.getId());
            Testcase saved = mongoTemplate.save(testcase);
            logger.info("Saved testcase: " + saved);
        }
        logger.info("Saved testcases ...");
    }


}
