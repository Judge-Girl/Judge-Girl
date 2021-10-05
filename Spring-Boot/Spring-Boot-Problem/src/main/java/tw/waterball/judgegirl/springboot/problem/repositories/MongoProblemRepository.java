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

package tw.waterball.judgegirl.springboot.problem.repositories;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.primitives.problem.*;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemQueryParams;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;
import tw.waterball.judgegirl.springboot.mongo.utils.MongoUtils;
import tw.waterball.judgegirl.springboot.problem.repositories.data.ProblemData;
import tw.waterball.judgegirl.springboot.profiles.productions.Mongo;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

import static java.lang.String.format;
import static java.nio.file.Files.createTempDirectory;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;
import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;
import static tw.waterball.judgegirl.commons.utils.StringUtils.isNullOrEmpty;
import static tw.waterball.judgegirl.commons.utils.ZipUtils.*;
import static tw.waterball.judgegirl.springboot.mongo.utils.MongoUtils.downloadFileResourceByFileId;
import static tw.waterball.judgegirl.springboot.problem.repositories.data.LanguageEnvData.toData;
import static tw.waterball.judgegirl.springboot.problem.repositories.data.ProblemData.toData;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Slf4j
@Mongo
@Component
public class MongoProblemRepository implements ProblemRepository {
    private static final int PAGE_SIZE = 50;
    private static final int OFFSET_NEW_PROBLEM_ID = 70000;
    private final MongoTemplate mongoTemplate;
    private final GridFsTemplate gridFsTemplate;

    public MongoProblemRepository(MongoTemplate mongoTemplate, GridFsTemplate gridFsTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.gridFsTemplate = gridFsTemplate;
    }

    @Override
    public Optional<Problem> findProblemById(int problemId) {
        return ofNullable(mongoTemplate.findById(problemId, ProblemData.class))
                .map(ProblemData::toEntity);
    }

    @Override
    public Optional<FileResource> downloadProvidedCodes(int problemId, String languageEnvName) {
        return MongoUtils.query(mongoTemplate)
                .fromDocument(ProblemData.class)
                .selectOneField(format("languageEnvs.%s.providedCodesFileId", languageEnvName))
                .byId(problemId)
                .execute()
                .getField(problem -> problem.getLanguageEnv(languageEnvName).getProvidedCodesFileId())
                .map(fileId -> downloadFileResourceByFileId(gridFsTemplate, fileId));
    }

    @Override
    public List<Problem> find(ProblemQueryParams params) {
        Query query = new Query();

        if (params.getTags().length > 0) {
            query.addCriteria(new Criteria("tags")
                    .all((Object[]) params.getTags()));
        }

        params.archivedFlag()
                .ifPresent(archivedFlag -> query.addCriteria(where("archived").is(archivedFlag)));

        boolean includeVisibleProblems = params.includeVisibleProblems();
        boolean includeInvisibleProblems = params.includeInvisibleProblems();
        if (includeVisibleProblems && !includeInvisibleProblems) {
            query.addCriteria(where("visible").is(true));
        } else if (!includeVisibleProblems && includeInvisibleProblems) {
            query.addCriteria(where("visible").is(false));
        }

        params.getPage()
                .map(Integer::longValue)
                .ifPresent(page -> query.skip(page * PAGE_SIZE).limit(PAGE_SIZE));

        return mapToList(mongoTemplate.find(query, ProblemData.class), ProblemData::toEntity);
    }

    @Override
    public int getPageSize() {
        return PAGE_SIZE;
    }

    @Override
    public List<Problem> findAll() {
        return mapToList(mongoTemplate.findAll(ProblemData.class), ProblemData::toEntity);
    }

    @Override
    public List<String> getTags() {
        AllTags tags = mongoTemplate.findOne(
                new Query(new Criteria()), AllTags.class);
        return tags == null ? Collections.emptyList() : tags.all;
    }

    @Override
    public Problem save(Problem problem) {
        if (problem.getId() == null) {
            problem.setId(autoIncrementId());
        }
        return mongoTemplate.save(toData(problem)).toEntity();
    }

    private int autoIncrementId() {
        int lastProblemId = ofNullable(mongoTemplate.findOne(
                new Query().with(Sort.by(Sort.Direction.DESC, "_id")).limit(1),
                ProblemData.class))
                .map(ProblemData::getId)
                .orElse(0);
        return Math.max(OFFSET_NEW_PROBLEM_ID, lastProblemId + 1);
    }

    @Override
    public boolean problemExists(int problemId) {
        return mongoTemplate.exists(query(where("_id").is(problemId)), ProblemData.class);
    }

    @Override
    public List<Problem> findProblemsByIds(int[] problemIds) {
        Object[] ids = Arrays.stream(problemIds).boxed().toArray(Object[]::new);
        return mapToList(mongoTemplate.find(query(where("_id").in(ids)), ProblemData.class), ProblemData::toEntity);
    }

    @Override
    public void archiveProblem(Problem problem) {
        Query query = query(where("_id").is(problem.getId()));
        Update update = update("archived", true);
        mongoTemplate.updateFirst(query, update, ProblemData.class);
    }

    @Override
    public void deleteProblem(Problem problem) {
        mongoTemplate.remove(query(where("_id").is(problem.getId())), ProblemData.class);
        deleteProvidedCodesAndTestcaseIOs(problem);
    }

    @Override
    public void deleteAll() {
        findAll().forEach(this::deleteProvidedCodesAndTestcaseIOs);
        mongoTemplate.dropCollection(ProblemData.class);
    }

    @Override
    public void deleteTestcaseById(int problemId, String testcaseId) {
        Query query = query(where("_id").is(problemId));
        Update update = new Update().unset("testcases." + testcaseId);
        mongoTemplate.updateFirst(query, update, ProblemData.class);
    }

    private void deleteProvidedCodesAndTestcaseIOs(Problem problem) {
        var languageEnvs = problem.getLanguageEnvs();

        List<String> providedCodesFileIds = mapToList(languageEnvs.values(), LanguageEnv::getProvidedCodesFileId);
        List<String> fileIds = new LinkedList<>(providedCodesFileIds);

        var testcaseIOsFileIds = problem.getTestcases().stream()
                .flatMap(testcase -> testcase.getTestcaseIO().stream())
                .map(TestcaseIO::getId).collect(toList());
        fileIds.addAll(testcaseIOsFileIds);
        if (!fileIds.isEmpty()) {
            gridFsTemplate.delete(query(where("_id").in(fileIds)));
        }
    }


    @Override
    public void saveTags(List<String> tagList) {
        mongoTemplate.save(new MongoProblemRepository.AllTags(tagList));
    }

    @Override
    public Problem save(Problem problem, Map<LanguageEnv, InputStream> providedCodesZipMap) {
        // TODO atomicity problem
        providedCodesZipMap.forEach((langEnv, zip) -> {
            String providedCodesName = format("%d-%s-provided.zip", problem.getId(), langEnv.getName());
            langEnv.setProvidedCodesFileId(
                    gridFsTemplate.store(zip, providedCodesName).toString());
        });
        return mongoTemplate.save(toData(problem)).toEntity();
    }


    @Override
    public Optional<FileResource> downloadTestCaseIOs(int problemId, String testcaseId) {
        return findProblemById(problemId)
                .flatMap(problem -> problem.getTestcaseById(testcaseId))
                .flatMap(Testcase::getTestcaseIO)
                .map(TestcaseIO::getId)
                .map(fileId -> downloadFileResourceByFileId(gridFsTemplate, fileId));
    }

    @SneakyThrows
    @Override
    public Problem patchTestcaseIOs(Problem problem, TestcaseIO.IoPatching ioPatching) {
        Testcase testcase = problem.getTestcaseById(ioPatching.getTestcaseId())
                .orElseThrow(() -> notFound(Testcase.class).id(ioPatching.getTestcaseId()));
        String originalIoFileId = testcase.getTestcaseIO().map(TestcaseIO::getId).orElse(null);

        InputStream zip = applyTestcaseIoPatching(testcase, ioPatching);
        String testcaseIOsName = format("%d-testcases-%s.zip", problem.getId(), testcase.getId());
        testcase.getTestcaseIO()
                .ifPresent(io -> {
                    String newIoFileId = gridFsTemplate.store(zip, testcaseIOsName).toString();
                    io.setId(newIoFileId);
                });
        removeFileByFileId(originalIoFileId);
        return save(problem);
    }

    @Override
    public void restoreProblem(Problem problem) {
        Query query = query(where("_id").is(problem.getId()));
        Update update = update("archived", false);
        mongoTemplate.updateFirst(query, update, ProblemData.class);
    }

    private InputStream applyTestcaseIoPatching(Testcase testcase,
                                                TestcaseIO.IoPatching ioPatching) throws IOException {
        Path tempDirPath = createTempDirectory("judgegirl");
        Path inDirPath = tempDirPath.resolve("in");
        Path outDirPath = tempDirPath.resolve("out");
        TestcaseIO testcaseIO = deduceTestcaseIoAfterPatching(testcase, ioPatching);

        setupTestcaseIoDir(testcase, tempDirPath, inDirPath, outDirPath);

        applyDeletions(inDirPath, ioPatching.getDeletedIns(), testcaseIO.mayHaveStdIn());
        applyDeletions(outDirPath, ioPatching.getDeletedOuts(), testcaseIO.mayHaveStdOut());

        patchStandardFile(inDirPath, ioPatching.getStdIn(),
                testcase.getTestcaseIO().map(TestcaseIO::getStdIn).orElse(null), testcaseIO.getStdIn());
        patchStandardFile(outDirPath, ioPatching.getStdOut(),
                testcase.getTestcaseIO().map(TestcaseIO::getStdOut).orElse(null), testcaseIO.getStdOut());

        patchIoFiles(inDirPath, ioPatching.getInputFiles());
        patchIoFiles(outDirPath, ioPatching.getOutputFiles());

        var baos = new ByteArrayOutputStream();
        zipFromFile(new File[]{inDirPath.toFile(), outDirPath.toFile()}, baos,
                name -> name.startsWith(".") /*ignore hidden files automatically generated by os*/);
        forceDelete(tempDirPath.toFile());
        testcase.setTestcaseIO(testcaseIO);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    @NotNull
    private TestcaseIO deduceTestcaseIoAfterPatching(Testcase testcase, TestcaseIO.IoPatching ioPatching) {
        return testcase.getTestcaseIO()
                .map(io -> io.apply(ioPatching))
                .orElseGet(ioPatching::toTestcaseIo);
    }

    private void setupTestcaseIoDir(Testcase testcase, Path testcaseIoHomeDir, Path inDirPath, Path outDirPath) throws IOException {
        var oldTestcaseIoOptional = downloadTestCaseIOs(testcase.getProblemId(), testcase.getId());

        if (oldTestcaseIoOptional.isPresent()) {
            unzipToDestination(oldTestcaseIoOptional.get().getInputStream(), testcaseIoHomeDir);
        } else {
            forceMkdir(inDirPath.toFile());
            forceMkdir(outDirPath.toFile());
        }
    }

    private void applyDeletions(Path dir, String[] deletedFileNames, Optional<String> standardFileName) throws IOException {
        for (String deletedIn : deletedFileNames) {
            // ignore the deletion of standard I/O
            if (!standardFileName.map(deletedIn::equals).orElse(false)) {
                try {
                    forceDelete(dir.resolve(deletedIn).toFile());
                } catch (FileNotFoundException ignored) {
                }
            }
        }
    }

    private void patchStandardFile(Path dir, Optional<FileResource> patchedStandardFile,
                                   @Nullable String oldStandardFileName, String newStandardFileName) {
        patchedStandardFile.ifPresent(stdFile -> {
            try {
                if (oldStandardFileName != null) {
                    forceDelete(dir.resolve(oldStandardFileName).toFile());
                }
                copyToFile(stdFile.getInputStream(),
                        dir.resolve(newStandardFileName).toFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void patchIoFiles(Path dir, Set<FileResource> ioFiles) throws IOException {
        for (FileResource file : ioFiles) {
            copyToFile(file.getInputStream(), dir.resolve(file.getFileName()).toFile());
        }
    }


    @Override
    public void uploadProvidedCodes(Problem problem, Language language, List<FileResource> providedCodes) {
        String fileId = saveProvidedCodesAndGetFileId(problem.getId(), language, providedCodes);
        updateProvidedCodesFileIdInProblem(problem, language, fileId);
    }

    private String saveProvidedCodesAndGetFileId(int problemId, Language language, List<FileResource> providedCodes) {
        String fileName = format("%d-%s-provided.zip", problemId, language.toString());
        ByteArrayInputStream zip = zipToStream(providedCodes);
        return gridFsTemplate.store(zip, fileName).toString();
    }

    private void updateProvidedCodesFileIdInProblem(Problem problem, Language language, String fileId) {
        var langEnv = problem.mayHaveLanguageEnv(language)
                .orElseThrow(() -> notFound(LanguageEnv.class).identifiedBy("language", language));
        String originalProvidedCodesFileId = langEnv.getProvidedCodesFileId();

        langEnv.setProvidedCodesFileId(fileId);
        updateLanguageEnv(problem.getId(), langEnv);

        removeFileByFileId(originalProvidedCodesFileId);
    }

    private void updateLanguageEnv(int problemId, LanguageEnv langEnv) {
        Update update = new Update();
        Query query = new Query(where("_id").is(problemId));
        update.set("languageEnvs." + langEnv.getLanguage(), toData(langEnv));
        mongoTemplate.upsert(query, update, ProblemData.class);
    }

    private void removeFileByFileId(String fileId) {
        if (!isNullOrEmpty(fileId)) {
            try {
                gridFsTemplate.delete(new Query(where("_id").is(fileId)));
            } catch (Exception err) {
                log.error("Error during removing a file from gridFs", err);
            }
        }
    }

    @Document("tag")
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllTags {
        public List<String> all;
    }

}