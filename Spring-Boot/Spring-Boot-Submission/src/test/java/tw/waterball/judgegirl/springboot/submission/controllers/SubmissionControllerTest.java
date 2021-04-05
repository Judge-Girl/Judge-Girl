package tw.waterball.judgegirl.springboot.submission.controllers;/*
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tw.waterball.judgegirl.commons.utils.Delay;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.entities.submission.Judge;
import tw.waterball.judgegirl.entities.submission.ProgramProfile;
import tw.waterball.judgegirl.entities.submission.Verdict;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.data.SubmissionData;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.data.VerdictData;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.submissionapi.views.VerdictIssuedEvent;
import tw.waterball.judgegirl.testkit.resultmatchers.ZipResultMatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.entities.problem.JudgeStatus.AC;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class SubmissionControllerTest extends AbstractSubmissionControllerTest {

    @Test
    void testSubmitAndThenDownload() throws Exception {
        SubmissionView submissionView = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN);

        requestGetSubmission(STUDENT1_ID, STUDENT1_TOKEN)
                .andExpect(content().json(
                        toJson(singletonList(submissionView))));

        requestDownloadSubmittedCodes(STUDENT1_ID, STUDENT1_TOKEN, submissionView.id, submissionView.submittedCodesFileId);
    }

    @DisplayName("When submit same submitted codes many times, " +
            "the submittedCodesFileId will be the same and the verdict is directly included as the response.")
    @Test
    void testVerdictShortcut() throws Exception {
        SubmissionView submissionView = submitCodeAndGet(ADMIN_ID, ADMIN_TOKEN, codes1);
        Verdict verdict = new Verdict(List.of(new Judge("T", AC, new ProgramProfile(10, 10, ""), 10)));
        submissionRepository.issueVerdictOfSubmission(submissionView.id, verdict);

        for (int i = 0; i < 3; i++) {
            SubmissionView duplicateSubmission = submitCodeAndGet(ADMIN_ID, ADMIN_TOKEN, codes1);
            assertEquals(submissionView.getSubmittedCodesFileId(), duplicateSubmission.getSubmittedCodesFileId());
            assertEquals(verdict.getSummaryStatus(), duplicateSubmission.verdict.getSummaryStatus());
            assertEquals(verdict.getTotalGrade(), duplicateSubmission.verdict.getTotalGrade());
        }

        // should deploy the judger only once, as other submissions are duplicate and have verdict-shortcuts
        verify(judgerDeployer, times(1)).deployJudger(any(), anyInt(), any());

        // different files --> should respond un-judged submission
        SubmissionView shouldBeUnJudged = submitCodeAndGet(ADMIN_ID, ADMIN_TOKEN, codes2);
        assertFalse(shouldBeUnJudged.isJudged);
    }


    // TODO: drunk code, need improving
    // A White-Box test: Strictly test the submission behavior
    @Test
    void WhenSubmitCodeWithValidToken_ShouldSaveIt_DeployJudger_ListenToVerdictIssuedEvent_AndHandleTheEvent() throws Exception {
        SubmissionView submissionView = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN);
        verifyJudgerDeployed(submissionView);

        // Publish the verdict through message queue after three seconds
        Delay.delay(3000);
        VerdictIssuedEvent verdictIssuedEvent = generateVerdictIssuedEvent(submissionView);
        verdictPublisher.publish(verdictIssuedEvent);

        // Verify the submission is updated with the verdict
        verdictIssuedEventHandler.onHandlingCompletion$.doWait(3000);
        SubmissionData updatedSubmissionData = mongoTemplate.findById(submissionView.getId(), SubmissionData.class);
        assertNotNull(updatedSubmissionData);
        VerdictData verdictData = updatedSubmissionData.getVerdict();
        assertEquals(50, verdictData.getTotalGrade());
        assertEquals(JudgeStatus.WA, verdictData.getSummaryStatus());
        assertEquals(new HashSet<>(verdictIssuedEvent.getVerdict().getJudges()),
                new HashSet<>(verdictData.getJudges()));
        assertEquals(stubReport.getRawData(), verdictData.getReportData());
    }

    @Test
    void GivenStudent1Submission_WhenGetThatSubmissionUsingAdminToken_ShouldRespondSuccessfully() throws Exception {
        SubmissionView submissionView = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN);

        // verify get submissions
        requestWithToken(() -> get(API_PREFIX, problem.getId(), STUDENT1_ID), ADMIN_TOKEN)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(toJson(singletonList(submissionView))));

        // verify download submitted codes
        requestWithToken(() -> get(API_PREFIX + "/{submissionId}/submittedCodes/{submittedCodesFileId}",
                problem.getId(), STUDENT1_ID, submissionView.getId(), submissionView.submittedCodesFileId), ADMIN_TOKEN)
                .andExpect(status().isOk())
                .andExpect(ZipResultMatcher.zip().content(codes1));
    }

    @Test
    void WhenGetStudent1SubmissionsWithStudent2Token_ShouldBeForbidden() throws Exception {
        requestWithToken(() -> get(API_PREFIX, problem.getId(),
                STUDENT1_ID), STUDENT2_TOKEN)
                .andExpect(status().isForbidden());
    }

    @Test
    void GivenStudent1Submission_WhenGetThatSubmissionUsingStudent2Token_ShouldRespondForbidden() throws Exception {
        SubmissionView submissionView = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN);
        requestWithToken(() -> get(API_PREFIX + "/{submissionId}",
                problem.getId(), STUDENT1_ID, submissionView.getId()), STUDENT2_TOKEN)
                .andExpect(status().isForbidden());
    }

    @Test
    void GivenStudent1Submission_WhenGetThatSubmissionUnderStudent2_ShouldRespondNotFound() throws Exception {
        SubmissionView submissionView = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN);
        requestWithToken(() -> get(API_PREFIX + "/{submissionId}",
                problem.getId(), STUDENT2_ID, submissionView.getId()), STUDENT2_TOKEN)
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenStudent1Submission_WhenDownloadItsSubmittedCodesUnderStudent2_ShouldRespondNotFound() throws Exception {
        SubmissionView submissionView = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN);
        requestWithToken(() -> get(API_PREFIX + "/{submissionId}/submittedCodes/{submittedCodesFileId}",
                problem.getId(), STUDENT2_ID, submissionView.getId(),
                submissionView.submittedCodesFileId), STUDENT2_TOKEN)
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenStudent1Submission_WhenDownloadItsSubmittedCodeUsingStudent2Token_ShouldBeForbidden() throws Exception {
        SubmissionView submissionView = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN);

        requestWithToken(() -> get(API_PREFIX + "/{submissionId}/submittedCodes/{submittedCodesFileId}",
                problem.getId(), STUDENT1_ID, submissionView.getId(), submissionView.submittedCodesFileId), STUDENT2_TOKEN)
                .andExpect(status().isForbidden());
    }

    @Test
    void GivenParallelSubmissions_WhenGetThoseSubmissionsInPage_ShouldReturnOnlyTheSubmissionsInThatPage() throws Exception {
        List<SubmissionView> submissionViews =
                givenParallelStudentSubmissions(STUDENT1_ID, 50);

        Set<SubmissionView> actualSubmissionsInPreviousPage = new HashSet<>();
        List<SubmissionView> actualSubmissions;
        List<SubmissionView> actualAllSubmissions = new ArrayList<>();

        int page = 0;
        do {
            actualSubmissions = getSubmissionsInPage(STUDENT1_ID, STUDENT1_TOKEN, page++);
            assertTrue(actualSubmissions.stream()
                    .noneMatch(actualSubmissionsInPreviousPage::contains));
            actualAllSubmissions.addAll(actualSubmissions);
            actualSubmissionsInPreviousPage = new HashSet<>(actualSubmissions);
        } while (!actualSubmissions.isEmpty());

        assertEquals(new HashSet<>(submissionViews), new HashSet<>(actualAllSubmissions));
    }

    @Test
    void WhenSubmitCodeUnderStudent1UsingStudent2Token_ShouldBeForbidden() throws Exception {
        submitCode(STUDENT1_ID, STUDENT2_TOKEN)
                .andExpect(status().isForbidden());
    }


}
