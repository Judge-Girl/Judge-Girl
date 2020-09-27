/*
 *  Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tw.waterball.judgegirl.springboot.submission.impl.repositories;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.models.files.StreamingResource;
import tw.waterball.judgegirl.commons.profiles.productions.Mongo;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.submissionservice.domain.repositories.SubmissionRepository;
import tw.waterball.judgegirl.entities.submission.SubmissionThrottling;
import tw.waterball.judgegirl.submissionservice.domain.usecases.dto.SubmissionQueryParams;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static tw.waterball.judgegirl.springboot.utils.MongoUtils.findOneFieldById;
import static tw.waterball.judgegirl.springboot.utils.MongoUtils.loadFileResourceByFileId;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Mongo
@Component
public class MongoSubmissionRepository implements SubmissionRepository {
    private final static int PAGE_SIZE = 30;
    private MongoTemplate mongoTemplate;
    private GridFsTemplate gridFsTemplate;

    public MongoSubmissionRepository(MongoTemplate mongoTemplate, GridFsTemplate gridFsTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.gridFsTemplate = gridFsTemplate;
    }

    @Override
    public Optional<Submission> findById(String submissionId) {
        return Optional.ofNullable(mongoTemplate.findById(submissionId, Submission.class));
    }

    @Override
    public List<Submission> findByJudgement(JudgeStatus judgeStatus) {
        List<Submission> submissions = mongoTemplate.findAll(Submission.class);
        if (judgeStatus == null || judgeStatus == JudgeStatus.NONE) {
            return submissions.stream().filter(s -> !s.isJudged())
                    .collect(Collectors.toList());
        }
        return submissions.stream()
                .filter(s -> s.getSummaryStatus() == judgeStatus).collect(Collectors.toList());
    }

    @Override
    public Submission save(Submission submission) {
        if (submission.isJudged() && submission.getSummaryStatus() == JudgeStatus.NONE) {
            throw new IllegalStateException(String.format("The submission (id=%s) is judged but its status is NONE, " +
                    "which is unexpected.", submission.getId()));
        }
        return mongoTemplate.save(submission);
    }

    @Override
    public String saveZippedSubmittedCodesAndGetFileId(StreamingResource streamingResource) throws IOException {
        return gridFsTemplate.store(streamingResource.getInputStream(), streamingResource.getFileName()).toString();
    }

    @Override
    public Optional<FileResource> findZippedSubmittedCodes(String submissionId) {
        return findOneFieldById(mongoTemplate,
                "zippedSubmittedCodeFilesId", Submission.class, submissionId,
                Submission::getZippedSubmittedCodeFilesId)
                .map((fileId) -> loadFileResourceByFileId(gridFsTemplate, fileId));
    }

    @Override
    public List<Submission> find(SubmissionQueryParams params) {
        Query query = new Query(Criteria.where("problemId").is(params.getProblemId())
                .and("studentId").is(params.getStudentId()));
        params.getPage().ifPresent(page -> query.skip(page * PAGE_SIZE).limit(PAGE_SIZE));
        return mongoTemplate.find(query, Submission.class);
    }

    @Override
    public Optional<SubmissionThrottling> findSubmissionThrottling(int problemId, int studentId) {
        return Optional.ofNullable(mongoTemplate.findOne(new Query(Criteria.where("studentId").is(studentId)
                .and("problemId").is(problemId)), SubmissionThrottling.class));
    }

    @Override
    public void saveSubmissionThrottling(SubmissionThrottling submissionThrottling) {
        mongoTemplate.save(submissionThrottling);
    }

}
