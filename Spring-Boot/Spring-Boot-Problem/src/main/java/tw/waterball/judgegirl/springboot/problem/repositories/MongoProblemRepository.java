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
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problem.domain.repositories.PatchProblemParams;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemQueryParams;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;
import tw.waterball.judgegirl.springboot.problem.repositories.data.ProblemData;
import tw.waterball.judgegirl.springboot.profiles.productions.Mongo;
import tw.waterball.judgegirl.springboot.utils.MongoUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;
import static tw.waterball.judgegirl.commons.utils.StringUtils.isNullOrEmpty;
import static tw.waterball.judgegirl.commons.utils.ZipUtils.zipToStream;
import static tw.waterball.judgegirl.springboot.problem.repositories.data.ProblemData.toData;
import static tw.waterball.judgegirl.springboot.utils.MongoUtils.downloadFileResourceByFileId;

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
    public Optional<FileResource> downloadTestCaseIOs(int problemId, String testcaseIOsFileId) {
        return MongoUtils.query(mongoTemplate)
                .fromDocument(Problem.class)
                .selectOneField("testcaseIOsFileId")
                .byId(problemId)
                .execute()
                .getField(Problem::getTestcaseIOsFileId)
                .map(fileId -> downloadFileResourceByFileId(gridFsTemplate, fileId));
    }

    @Override
    public Optional<Problem> findProblemById(int problemId) {
        return Optional.ofNullable(mongoTemplate.findById(problemId, ProblemData.class))
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
        if (params.isExcludeArchive()) {
            query.addCriteria(where("archived").is(false));
        }
        if (!params.isIncludeInvisibleProblems()) {
            query.addCriteria(where("visible").is(true));
        }
        params.getPage().ifPresent(page -> query.skip(page * PAGE_SIZE).limit(PAGE_SIZE));

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
    public Problem save(Problem problem, Map<LanguageEnv, InputStream> providedCodesZipMap, InputStream testcaseIOsZip) {
        // TODO atomicity problem
        providedCodesZipMap.forEach((langEnv, zip) -> {
            String providedCodesName = format("%d-%s-provided.zip", problem.getId(), langEnv.getName());
            langEnv.setProvidedCodesFileId(
                    gridFsTemplate.store(zip, providedCodesName).toString());
        });
        String testcaseIOsName = format("%d-testcases.zip", problem.getId());
        problem.setTestcaseIOsFileId(
                gridFsTemplate.store(testcaseIOsZip, testcaseIOsName).toString());
        return mongoTemplate.save(toData(problem)).toEntity();
    }

    @Override
    public Problem save(Problem problem) {
        return mongoTemplate.save(toData(problem)).toEntity();
    }

    @Override
    public int saveProblemWithTitleAndGetId(String title) {
        long problemsCount = mongoTemplate.count(
                query(where("title").exists(true)), ProblemData.class);
        int id = (int) (OFFSET_NEW_PROBLEM_ID + problemsCount + 1);
        Problem problem = Problem.builder()
                .id(id)
                .title(title)
                .visible(false)
                .build();
        Problem saved = mongoTemplate.save(toData(problem)).toEntity();
        log.info("New problem with title {} has been saved with id={}.",
                saved.getTitle(), saved.getId());
        return id;
    }

    @Override
    public void patchProblem(int problemId, PatchProblemParams params) {
        Update update = new Update();
        Query query = new Query(where("_id").is(problemId));
        params.getTitle().ifPresent(title -> update.set("title", title));
        params.getDescription().ifPresent(des -> update.set("description", des));
        params.getMatchPolicyPluginTag().ifPresent(tag -> update.set("outputMatchPolicyPluginTag", tag));
        params.getFilterPluginTags().ifPresent(tags -> update.set("filterPluginTags", tags));
        params.getLanguageEnv().ifPresent(languageEnv -> update.set("languageEnvs." + languageEnv.getLanguage(), languageEnv));

        params.getTestcase().ifPresent(tc -> update.set("testcases." + tc.getId(), tc));

        if (!update.getUpdateObject().isEmpty()) {
            mongoTemplate.upsert(query, update, ProblemData.class);
        }
    }

    @Override
    public boolean problemExists(int problemId) {
        return mongoTemplate.exists(query(where("_id").is(problemId)), ProblemData.class);
    }

    @Override
    public boolean isFileExistsById(String fileId) {
        try {
            GridFSFile gridFsFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(fileId)));
            gridFsTemplate.getResource(requireNonNull(gridFsFile));
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Problem> findProblemsByIds(int[] problemIds) {
        Integer[] ids = Arrays.stream(problemIds).boxed().toArray(Integer[]::new);
        return mapToList(mongoTemplate.find(query(where("_id").in(ids)), ProblemData.class), ProblemData::toEntity);
    }

    @Override
    public void archiveProblem(Problem problem) {
        Update update = Update.update("archived", true);
        Query query = new Query(where("_id").is(problem.getId()));
        mongoTemplate.updateFirst(query, update, ProblemData.class);
    }

    @Override
    public void deleteProblem(Problem problem) {
        mongoTemplate.remove(query(where("_id").is(problem.getId())), ProblemData.class);
        List<String> fileIds = new ArrayList<>();
        if (problem.getLanguageEnvs() != null) {
            var providedCodesFileIds = mapToList(problem.getLanguageEnvs().values(), LanguageEnv::getProvidedCodesFileId);
            fileIds.addAll(providedCodesFileIds);
        }
        if (problem.getTestcaseIOsFileId() != null) {
            fileIds.add(problem.getTestcaseIOsFileId());
        }
        if(!fileIds.isEmpty()){
            gridFsTemplate.delete(query(where("_id").in(fileIds)));
        }
    }


    @Override
    public void deleteAll() {
        List<Problem> problems = mapToList(mongoTemplate.findAll(ProblemData.class), ProblemData::toEntity);
        List<String> fileIds = new ArrayList<>();
        problems.forEach(problem -> {
            if (problem.getLanguageEnvs() != null) {
                List<String> providedCodesFileIds = mapToList(problem.getLanguageEnvs().values(), LanguageEnv::getProvidedCodesFileId);
                fileIds.addAll(providedCodesFileIds);
            }
            if (problem.getTestcaseIOsFileId() != null) {
                fileIds.add(problem.getTestcaseIOsFileId());
            }
        });
        if(!fileIds.isEmpty()){
            gridFsTemplate.delete(query(where("_id").in(fileIds)));
        }
        mongoTemplate.dropCollection(ProblemData.class);
    }

    @Override
    public void saveTags(List<String> tagList) {
        mongoTemplate.save(new MongoProblemRepository.AllTags(tagList));
    }

    @Override
    public void updateProblemWithProvidedCodes(Problem problem, Language language, List<FileResource> providedCodes) {
        String fileId = saveProvidedCodesAndGetFileId(problem.getId(), language, providedCodes);
        updateProvidedCodesFileIdInProblem(problem, language, fileId);
    }

    private String saveProvidedCodesAndGetFileId(int problemId, Language language, List<FileResource> providedCodes) {
        String fileName = format("%d-%s-provided.zip", problemId, language.toString());
        ByteArrayInputStream zip = zipToStream(providedCodes);
        return gridFsTemplate.store(zip, fileName).toString();
    }

    // TODO: Transaction: Operations in `updateProvidedCodesFileIdInProblem` should be atomic
    private void updateProvidedCodesFileIdInProblem(Problem problem, Language language, String fileId) {
        problem.mayHaveLanguageEnv(language)
                .ifPresentOrElse(langEnv -> {
                    removeFileIdIfExists(langEnv.getProvidedCodesFileId());
                    langEnv.setProvidedCodesFileId(fileId);
                    updateLanguageEnv(problem.getId(), langEnv, language);
                }, () -> {
                    LanguageEnv langEnv = LanguageEnv.builder()
                            .language(language)
                            .providedCodesFileId(fileId)
                            .build();
                    problem.addLanguageEnv(langEnv);
                    updateLanguageEnv(problem.getId(), langEnv, language);
                });
    }

    private void removeFileIdIfExists(String fileId) {
        if (isNullOrEmpty(fileId)) {
            gridFsTemplate.delete(new Query(where("_id").is(fileId)));
        }
    }

    private void updateLanguageEnv(int problemId, LanguageEnv langEnv, Language language) {
        Update update = new Update();
        Query query = new Query(where("_id").is(problemId));
        update.set("languageEnvs." + language, langEnv);
        mongoTemplate.upsert(query, update, ProblemData.class);
    }

    @Document("tag")
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllTags {
        public List<String> all;
    }

}