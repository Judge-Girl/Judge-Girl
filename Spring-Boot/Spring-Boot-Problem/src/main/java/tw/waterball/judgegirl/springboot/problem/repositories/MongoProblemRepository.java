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

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.entities.problem.LanguageEnv;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemQueryParams;
import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemRepository;
import tw.waterball.judgegirl.springboot.profiles.productions.Mongo;
import tw.waterball.judgegirl.springboot.utils.MongoUtils;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static tw.waterball.judgegirl.springboot.utils.MongoUtils.downloadFileResourceByFileId;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Slf4j
@Mongo
@Component
public class MongoProblemRepository implements ProblemRepository {
    private final static int PAGE_SIZE = 50;
    private final static int OFFSET_NEW_PROBLEM_ID = 70000;
    private final MongoTemplate mongoTemplate;
    private final GridFsTemplate gridFsTemplate;

    public MongoProblemRepository(MongoTemplate mongoTemplate, GridFsTemplate gridFsTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.gridFsTemplate = gridFsTemplate;
    }

    @Override
    public Optional<Problem> findProblemById(int problemId) {
        return Optional.ofNullable(mongoTemplate.findById(problemId, Problem.class));
    }

    @Override
    public Optional<FileResource> downloadProvidedCodes(int problemId, String languageEnvName) {
        return MongoUtils.query(mongoTemplate)
                .fromDocument(Problem.class)
                .selectOneField(format("languageEnvs.%s.providedCodesFileId", languageEnvName))
                .byId(problemId)
                .execute()
                .getField(problem -> problem.getLanguageEnv(languageEnvName).getProvidedCodesFileId())
                .map((fileId) -> downloadFileResourceByFileId(gridFsTemplate, fileId));
    }

    @Override
    public List<Problem> find(ProblemQueryParams params) {
        Query query = new Query();

        if (params.getTags().length > 0) {
            query.addCriteria(new Criteria("tags")
                    .all((Object[]) params.getTags()));
        }
        params.getPage().ifPresent(page -> query.skip(page * PAGE_SIZE).limit(PAGE_SIZE));

        return mongoTemplate.find(query, Problem.class);
    }

    @Override
    public int getPageSize() {
        return PAGE_SIZE;
    }

    @Override
    public List<Problem> findAll() {
        return mongoTemplate.findAll(Problem.class);
    }

    @Override
    public List<String> getTags() {
        AllTags tags = mongoTemplate.findOne(
                new Query(new Criteria()), AllTags.class);
        return tags == null ? Collections.emptyList() : tags.all;
    }

    @Override
    public Problem save(Problem problem, Map<LanguageEnv, InputStream> providedCodesZipMap, InputStream testcaseIOsZip) {
        // TODO: atomicity doesn't hold
        providedCodesZipMap.forEach((langEnv, zip) -> {
            String providedCodesName = format("%d-%s-provided.zip", problem.getId(), langEnv.getName());
            langEnv.setProvidedCodesFileId(
                    gridFsTemplate.store(zip, providedCodesName).toString());
        });
        String testcaseIOsName = format("%d-testcases.zip", problem.getId());
        problem.setTestcaseIOsFileId(
                gridFsTemplate.store(testcaseIOsZip, testcaseIOsName).toString());
        return mongoTemplate.save(problem);
    }

    @Override
    public int saveProblemWithTitleAndGetId(String title) {
        long problemsCount = mongoTemplate.count(
                query(where("title").exists(true)), Problem.class);
        int id = (int) (OFFSET_NEW_PROBLEM_ID + problemsCount + 1);
        Problem problem = Problem.builder()
                .id(id)
                .title(title)
                .visible(false)
                .build();
        Problem saved = mongoTemplate.save(problem);
        log.info("New problem with title {} has been saved with id={}.",
                saved.getTitle(), saved.getId());
        return id;
    }

    @Document("tag")
    public static class AllTags {
        public List<String> all;

        public AllTags() {
        }

        public AllTags(List<String> all) {
            this.all = all;
        }
    }

}