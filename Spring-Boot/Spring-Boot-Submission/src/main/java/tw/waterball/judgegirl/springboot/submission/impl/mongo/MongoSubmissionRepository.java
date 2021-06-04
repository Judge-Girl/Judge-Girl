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

package tw.waterball.judgegirl.springboot.submission.impl.mongo;

import com.mongodb.client.result.UpdateResult;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.models.files.StreamingResource;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.primitives.submission.SubmissionThrottling;
import tw.waterball.judgegirl.primitives.submission.verdict.Verdict;
import tw.waterball.judgegirl.springboot.mongo.utils.MongoUtils;
import tw.waterball.judgegirl.springboot.profiles.productions.Mongo;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.data.DataMapper;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.data.SubmissionData;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.data.VerdictData;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.strategy.SaveSubmissionWithCodesStrategy;
import tw.waterball.judgegirl.submission.domain.repositories.SubmissionRepository;
import tw.waterball.judgegirl.submission.domain.usecases.query.SubmissionQueryParams;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Update.update;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;
import static tw.waterball.judgegirl.springboot.mongo.utils.MongoUtils.downloadFileResourceByFileId;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Mongo
@Component
@AllArgsConstructor
public class MongoSubmissionRepository implements SubmissionRepository {
    private static final int PAGE_SIZE = 80;
    private final MongoTemplate mongoTemplate;
    private final GridFsTemplate gridFsTemplate;
    private final SaveSubmissionWithCodesStrategy saveSubmissionWithCodesStrategy;

    @Override
    public List<Submission> findByProblemIdAndJudgeStatus(int problemId, JudgeStatus judgeStatus) {
        var data = mongoTemplate.find(Query.query(where("problemId").is(problemId)
                .and("verdict.summaryStatus").is(judgeStatus.toString())), SubmissionData.class);
        return DataMapper.toEntity(data);
    }

    @Override
    public Optional<Submission> findOne(int studentId, String submissionId) {
        SubmissionData data = mongoTemplate.findOne(
                Query.query(where("studentId").is(studentId)
                        .and("id").is(submissionId)), SubmissionData.class);
        return ofNullable(DataMapper.toEntity(data));
    }

    @Override
    public void issueVerdict(String submissionId, Verdict verdict) {
        VerdictData verdictData = DataMapper.toData(verdict);
        UpdateResult result = mongoTemplate.updateFirst(
                Query.query(where("id").is(submissionId)),
                update("verdict", verdictData), SubmissionData.class);

        assert result.getModifiedCount() == 1 : "Assert only one submission will be issued its verdict.";
    }

    @Override
    public List<Submission> findBySummaryJudgeStatus(JudgeStatus summaryJudgeStatus) {
        return null; // TODO implement
    }

    @Override
    public Submission saveSubmissionWithCodes(Submission submission, List<FileResource> originalCodes) {
        String fileName = format("%d-%s-%d.zip", submission.getStudentId(), submission.getProblemId(), System.currentTimeMillis());
        return saveSubmissionWithCodesStrategy.perform(submission, originalCodes, fileName);
    }

    @Override
    public Submission save(Submission submission) {
        SubmissionData data = DataMapper.toData(submission);
        data = mongoTemplate.save(data);
        return DataMapper.toEntity(data);
    }

    @Override
    public String saveZippedSubmittedCodesAndGetFileId(StreamingResource streamingResource) {
        return gridFsTemplate.store(streamingResource.getInputStream(),
                streamingResource.getFileName()).toString();
    }

    @Override
    public Optional<FileResource> downloadZippedSubmittedCodes(String submissionId) {
        return MongoUtils.query(mongoTemplate)
                .fromDocument(SubmissionData.class)
                .selectOneField("submittedCodesFileId")
                .byId(submissionId)
                .execute()
                .getField(SubmissionData::getSubmittedCodesFileId)
                .map(fileId -> downloadFileResourceByFileId(gridFsTemplate, fileId));
    }

    @Override
    public List<Submission> query(SubmissionQueryParams params) {
        var criteria = new Criteria();
        params.getProblemId()
                .ifPresent(problemId -> criteria.and("problemId").is(problemId));
        params.getLanguageEnvName()
                .ifPresent(langEnvName -> criteria.and("languageEnvName").is(langEnvName));
        params.getStudentId()
                .ifPresent(studentId -> criteria.and("studentId").is(studentId));
        params.getBagQueryParameters().forEach((key, val) -> criteria.and("bag." + key).is(val));

        Query query = Query.query(criteria);
        params.getSortBy()
                .ifPresent(sortBy -> query.with(
                        Sort.by(sortBy.isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC,
                                sortBy.getFieldName().equals("id") ? "_id" : sortBy.getFieldName())));
        params.getPage().ifPresent(page -> query.skip(page * PAGE_SIZE).limit(PAGE_SIZE));
        List<SubmissionData> dataList = mongoTemplate.find(query, SubmissionData.class);
        return DataMapper.toEntity(dataList);
    }

    @Override
    public Optional<Submission> findById(String submissionId) {
        return ofNullable(mongoTemplate.findById(submissionId, SubmissionData.class))
                .map(DataMapper::toEntity);

    }

    @Override
    public Optional<SubmissionThrottling> findSubmissionThrottling(int problemId, int studentId) {
        return ofNullable(mongoTemplate.findOne(
                Query.query(where("studentId").is(studentId)
                        .and("problemId").is(problemId)), SubmissionThrottling.class));
    }

    @Override
    public void saveSubmissionThrottling(SubmissionThrottling submissionThrottling) {
        mongoTemplate.save(submissionThrottling);
    }

    @Override
    public boolean submissionExists(String submissionId) {
        return mongoTemplate.exists(Query.query(where("id").is(submissionId)), SubmissionData.class);
    }

    @Override
    public List<Submission> findAllByIds(String... submissionIds) {
        List<String> ids = asList(submissionIds);
        var dataList = mongoTemplate.find(
                Query.query(where("id").in(ids)), SubmissionData.class);
        return mapToList(dataList, DataMapper::toEntity);
    }

}
