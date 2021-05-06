package tw.waterball.judgegirl.springboot.submission.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.data.SampleSubmissionData;
import tw.waterball.judgegirl.submission.domain.repositories.SampleRepository;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author - wally55077@gmail.com
 */
public class SampleSubmissionControllerTest extends AbstractSubmissionControllerTest {

    private static final String PROBLEM_PATH = "/api/problems";
    private static final String SUBMISSION_PATH = "/api/submissions";

    @Autowired
    private SampleRepository sampleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @AfterEach
    void dropSampleSubmissions() {
        mongoTemplate.dropCollection(SampleSubmissionData.class);
    }

    @Test
    public void GivenOneProblemAndTwoSubmissions_WhenUpgradeTwoSubmissionsToSamples_ThenSamplesQueryShouldRespondTwoSubmissions() throws Exception {
        int problemId = problem.getId();
        String submissionId1 = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN).getId();
        String submissionId2 = submitCodeAndGet(STUDENT2_ID, STUDENT2_TOKEN).getId();

        upgradeSubmissionsToSamples(submissionId1, submissionId2);

        samplesQueryShouldRespondSubmissions(problemId, submissionId1, submissionId2);
    }

    @Test
    public void GivenTwoSampleSubmissionsUpgraded_WhenGetSamples_ThenShouldRespondTwoSamples() throws Exception {
        int problemId = problem.getId();
        SubmissionView submission1 = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN);
        SubmissionView submission2 = submitCodeAndGet(STUDENT2_ID, STUDENT2_TOKEN);
        upgradeSubmissionsToSamples(submission1.getId(), submission2.getId());

        var samples = getSamples(problemId);

        samplesQueryShouldRespondSubmissions(samples, submission1, submission2);
    }

    @Test
    public void GivenTwoSampleSubmissionsUpgraded_WhenDowngradeOneSampleBackToSubmission_ThenSamplesQueryShouldRespondOneSubmission() throws Exception {
        int problemId = problem.getId();
        String submissionId1 = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN).getId();
        String submissionId2 = submitCodeAndGet(STUDENT2_ID, STUDENT2_TOKEN).getId();
        upgradeSubmissionsToSamples(submissionId1, submissionId2);

        downgradeSampleBackToSubmission(submissionId1);

        samplesQueryShouldRespondSubmissions(problemId, submissionId2);
    }

    private void upgradeSubmissionsToSamples(String... submissionIds) throws Exception {
        for (String submissionId : submissionIds) {
            upgradeSubmissionToSample(submissionId);
        }
    }

    private void upgradeSubmissionToSample(String submissionId) throws Exception {
        mockMvc.perform(post(SUBMISSION_PATH + "/{submissionId}/sample", submissionId))
                .andExpect(status().isOk());
    }

    private List<SubmissionView> getSamples(int problemId) throws Exception {
        return getBody(mockMvc.perform(get(PROBLEM_PATH + "/{problemId}/samples",
                problemId)).andExpect(status().isOk()), new TypeReference<>() {
        });
    }

    private void downgradeSampleBackToSubmission(String submissionId) throws Exception {
        mockMvc.perform(delete(SUBMISSION_PATH + "/{submissionId}/sample", submissionId))
                .andExpect(status().isOk());
    }

    private void samplesQueryShouldRespondSubmissions(int problemId, String... submissionIds) {
        var sampleSubmissionIds = sampleRepository.findSampleSubmissionIds(problemId);
        assertEquals(submissionIds.length, sampleSubmissionIds.size());
        IntStream.range(0, submissionIds.length)
                .forEach(index -> assertEquals(submissionIds[index], sampleSubmissionIds.get(index)));
    }

    private void samplesQueryShouldRespondSubmissions(List<SubmissionView> samples, SubmissionView... submissions) {
        assertEquals(submissions.length, samples.size());
        for (int index = 0; index < submissions.length; index++) {
            assertEquals(submissions[index], samples.get(index));
        }
    }

}