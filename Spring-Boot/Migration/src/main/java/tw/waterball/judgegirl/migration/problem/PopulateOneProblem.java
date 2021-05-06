package tw.waterball.judgegirl.migration.problem;

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

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import tw.waterball.judgegirl.commons.utils.ArrayUtils;
import tw.waterball.judgegirl.commons.utils.ZipUtils;
import tw.waterball.judgegirl.commons.utils.functional.ErrConsumer;
import tw.waterball.judgegirl.plugins.api.JudgeGirlPlugin;
import tw.waterball.judgegirl.plugins.api.match.JudgeGirlMatchPolicyPlugin;
import tw.waterball.judgegirl.plugins.impl.match.AllMatchPolicyPlugin;
import tw.waterball.judgegirl.plugins.impl.match.RegexMatchPolicyPlugin;
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.problem.ResourceSpec;
import tw.waterball.judgegirl.springboot.problem.repositories.data.ProblemData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;
import static tw.waterball.judgegirl.migration.problem.NewJudgeGirlLayoutManipulator.PROVIDED_CODES_DIR;
import static tw.waterball.judgegirl.migration.problem.NewJudgeGirlLayoutManipulator.TESTCASES_HOME;
import static tw.waterball.judgegirl.springboot.problem.repositories.data.ProblemData.toData;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@SuppressWarnings("SameParameterValue")
@SpringBootApplication
@RequiredArgsConstructor
public class PopulateOneProblem {
    public static final Language DEFAULT_LANGUAGE = Language.C;
    public static final String PROVIDED_CODES_FILE_NAME_PATTERN = "%d-%s-provided.zip";
    public static final String TESTCASES_IO_FILE_NAME_PATTERN = "%d-testcases.zip";
    private static final Logger logger = LogManager.getLogger();

    private final Input in;
    private final MongoTemplate mongoTemplate;
    private final GridFsTemplate gridFsTemplate;
    private final NewJudgeGirlLayoutManipulator layoutManipulator;
    private final JudgeGirlMatchPolicyPlugin[] matchPolicyPlugins =
            {new AllMatchPolicyPlugin(), new RegexMatchPolicyPlugin()};

    private Problem problem;

    public static void main(String[] args) {
        var context = SpringApplication.run(MigrateOneProblem.class, args);
        try {
            PopulateOneProblem populateOneProblem = context.getBean(PopulateOneProblem.class);
            populateOneProblem.execute();
            System.out.println("Completed.");
            SpringApplication.exit(context, () -> 1);
        } catch (Exception err) {
            err.printStackTrace();
            SpringApplication.exit(context, () -> -1);
        }
    }

    public void execute() throws Exception {
        Path problemDirPath = Paths.get(in.problemDirPath());
        layoutManipulator.verifyProblemDirLayout(problemDirPath);
        problem = layoutManipulator.verifyAndReadProblem(problemDirPath);
        populateProblemAndTestcasesWithNewSchema();
        migrateToJudgeGirlDatabase(problemDirPath);
    }

    private void populateProblemAndTestcasesWithNewSchema() {
        LanguageEnv langEnv = problem.getLanguageEnv(DEFAULT_LANGUAGE);
        langEnv.setResourceSpec(in.resourceSpec());
        problem.setOutputMatchPolicyPluginTag(
                in.matchPolicyPlugin(matchPolicyPlugins).getTag());
    }

    private void migrateToJudgeGirlDatabase(Path problemDirPath) {
        logger.info("Migrating ...");

        List<Problem> problems = mapToList(mongoTemplate.findAll(ProblemData.class), ProblemData::toEntity);
        replaceExistingProblemIfAgree(problems);

        try {
            saveProvidedCodes(problemDirPath);
            saveTestcaseIOs(problemDirPath);
        } catch (Exception err) {
            err.printStackTrace();
        }

        // save problem only after so the fileIds have been updated
        saveProblem();

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
        mongoTemplate.findAndRemove(query(where("_id").is(problem.getId())), ProblemData.class);
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


    private void saveTestcaseIOs(Path problemDirPath) throws Exception {
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
        ZipUtils.zipFromFile(files, new FileOutputStream(zipOutputFile), ignoredFileNames);
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
        problem = mongoTemplate.save(toData(problem)).toEntity();
        logger.info("Saved problem, " + problem);
    }


    public interface Input {

        String problemDirPath();

        ResourceSpec resourceSpec();

        JudgeGirlPlugin matchPolicyPlugin(JudgeGirlMatchPolicyPlugin[] matchPolicyPlugins);

        Optional<Problem> replaceExistingProblemOrNot(List<Problem> problems);

        OptionalInt specifyProblemIdOrNot(List<Problem> problems);
    }


}

