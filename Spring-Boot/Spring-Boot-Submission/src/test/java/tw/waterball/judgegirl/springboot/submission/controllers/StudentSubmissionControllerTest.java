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
package tw.waterball.judgegirl.springboot.submission.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.primitives.submission.events.LiveSubmissionEvent;
import tw.waterball.judgegirl.submission.domain.usecases.RejudgeSubmissionsUseCase;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.submissionapi.views.VerdictView;
import tw.waterball.judgegirl.testkit.resultmatchers.ZipResultMatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.primitives.problem.JudgeStatus.AC;
import static tw.waterball.judgegirl.primitives.stubs.SubmissionStubBuilder.submission;
import static tw.waterball.judgegirl.primitives.stubs.VerdictStubBuilder.verdict;
import static tw.waterball.judgegirl.submissionapi.views.VerdictView.toViewModel;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class StudentSubmissionControllerTest extends AbstractSubmissionControllerTest {

    @Test
    void testSubmitAndThenDownload() throws Exception {
        var submission = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN);

        requestGetSubmission(STUDENT1_ID, STUDENT1_TOKEN)
                .andExpect(content().json(
                        toJson(singletonList(submission))));

        requestDownloadSubmittedCodes(STUDENT1_ID, STUDENT1_TOKEN, submission.id, submission.submittedCodesFileId);
    }

    @Test
    void WhenSubmitCode_ShouldPublishNewLiveSubmission() throws Exception {
        var submission = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN);

        var event = new LiveSubmissionEvent(submission.problemId, submission.languageEnvName,
                submission.studentId, submission.id, submission.submissionTime, new Bag(submission.getBag()));

        verify(eventPublisher).publish(event);
    }

    @Test
    @Timeout(5)
    void WhenSubmitCodeWithAdminToken_ShouldCompleteJudgeFlow_SaveAndBringSubmissionBagToJudger() throws Exception {
        SubmissionView submission = submitCodeAndGet(ADMIN_ID, ADMIN_TOKEN);
        assertThat("Admin's submission should have bag in the responded submission",
                submissionBag.entrySet(), everyItem(is(in(submission.getBag().entrySet()))));

        VerdictView verdict = toViewModel(verdict()
                .AC(5, 5, 20)
                .AC(6, 6, 30)
                .WA(7, 7).build());
        shouldCompleteJudgeFlow(submission, verdict,
                JudgeStatus.WA, 50, shouldBringSubmissionBagToJudger());

        var savedSubmission = submissionRepository.findById(submission.id).orElseThrow();
        assertThat("The submission's bag should have been saved",
                submissionBag.entrySet(), everyItem(is(in(savedSubmission.getBag().entrySet()))));
    }

    @Test
    @Timeout(5)
    void WhenSubmitCodeWithValidToken_ShouldCompleteJudgeFlow() throws Exception {
        SubmissionView submission = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN);

        VerdictView verdict = toViewModel(verdict()
                .AC(5, 5, 20)
                .AC(6, 6, 30)
                .WA(7, 7).build());
        shouldCompleteJudgeFlow(submission, verdict, JudgeStatus.WA, 50);
    }


    @Test
    void GivenStudent1Submission_WhenGetThatSubmissionUsingAdminToken_ShouldRespondSuccessfully() throws Exception {
        SubmissionView submissionView = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN);

        // verify get submissions
        mockMvc.perform(withToken(ADMIN_TOKEN,
                get(API_PREFIX, problem.getId(), STUDENT1_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(toJson(singletonList(submissionView))));

        // verify download submitted codes
        mockMvc.perform(withToken(ADMIN_TOKEN,
                get(API_PREFIX + "/{submissionId}/submittedCodes/{submittedCodesFileId}",
                        problem.getId(), STUDENT1_ID, submissionView.getId(), submissionView.submittedCodesFileId)))
                .andExpect(status().isOk())
                .andExpect(ZipResultMatcher.zip().content(codes1));
    }

    @Test
    void WhenGetStudent1SubmissionsWithStudent2Token_ShouldBeForbidden() throws Exception {
        mockMvc.perform(withToken(STUDENT2_TOKEN,
                get(API_PREFIX, problem.getId(), STUDENT1_ID)))
                .andExpect(status().isForbidden());
    }

    @Test
    void GivenStudent1Submission_WhenGetThatSubmissionUsingStudent2Token_ShouldRespondForbidden() throws Exception {
        SubmissionView submissionView = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN);
        mockMvc.perform(withToken(STUDENT2_TOKEN,
                get(API_PREFIX + "/{submissionId}",
                        problem.getId(), STUDENT1_ID, submissionView.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void GivenStudent1Submission_WhenGetThatSubmissionUnderStudent2_ShouldRespondNotFound() throws Exception {
        SubmissionView submissionView = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN);
        mockMvc.perform(withToken(STUDENT2_TOKEN,
                get(API_PREFIX + "/{submissionId}",
                        problem.getId(), STUDENT2_ID, submissionView.getId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenStudent1Submission_WhenDownloadItsSubmittedCodesUnderStudent2_ShouldRespondNotFound() throws Exception {
        SubmissionView submissionView = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN);
        mockMvc.perform(withToken(STUDENT2_TOKEN,
                get(API_PREFIX + "/{submissionId}/submittedCodes/{submittedCodesFileId}",
                        problem.getId(), STUDENT2_ID, submissionView.getId(),
                        submissionView.submittedCodesFileId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenStudent1Submission_WhenDownloadItsSubmittedCodeUsingStudent2Token_ShouldBeForbidden() throws Exception {
        SubmissionView submissionView = submitCodeAndGet(STUDENT1_ID, STUDENT1_TOKEN);

        mockMvc.perform(withToken(STUDENT2_TOKEN,
                get(API_PREFIX + "/{submissionId}/submittedCodes/{submittedCodesFileId}",
                        problem.getId(), STUDENT1_ID, submissionView.getId(), submissionView.submittedCodesFileId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void GivenSubmissionsSubmittedConcurrently_WhenGetThoseSubmissionsInPage_ShouldReturnOnlyTheSubmissionsInThatPage() throws Exception {
        List<SubmissionView> submissionViews =
                givenStudentSubmissionsSubmittedConcurrently(STUDENT1_ID, 50);

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

    @DisplayName("Given submissions with bag messages A(a=1,b=1), B(a=1,b=2), C(a=2,b=2), " +
            "When get submissions with bag query parameters (a=1 & b=2), " +
            "Should only respond B")
    @Test
    void testGetSubmissionsWithBagQueryParameters() throws Exception {
        givenSubmission(submission("A").CE(100).bag("a", "1").bag("b", "1").build(ADMIN_ID, problem.getId(), Language.C.toString()));
        givenSubmission(submission("B").bag("a", "1").bag("b", "2").build(ADMIN_ID, problem.getId(), Language.C.toString()));
        givenSubmission(submission("C").bag("a", "2").bag("b", "2").build(ADMIN_ID, problem.getId(), Language.C.toString()));

        var bagQueryParameters = new LinkedMultiValueMap<String, String>();
        bagQueryParameters.add("a", "1");
        bagQueryParameters.add("b", "2");

        var submissions = getSubmissionsWithBagQuery(ADMIN_ID, ADMIN_TOKEN, bagQueryParameters);
        submissionsShouldHaveIds(submissions, "B");
    }

    @Test
    void WhenSubmitCodeUnderStudent1UsingStudent2Token_ShouldBeForbidden() throws Exception {
        submitCode(STUDENT1_ID, STUDENT2_TOKEN)
                .andExpect(status().isForbidden());
    }

    // todo should fix
//    @DisplayName("[Verdict-Shortcut] When submit same submitted codes many times, " +
//            "the submittedCodesFileId will be the same and the verdict is directly included as the response.")
//    @Test
//    void testVerdictShortcut() throws Exception {
//        SubmissionView submissionView = submitCodeAndGet(ADMIN_ID, ADMIN_TOKEN, codes1);
//        Verdict verdict = new Verdict(List.of(new Judge("T", AC, new ProgramProfile(10, 10, ""), 10)));
//        submissionRepository.issueVerdictOfSubmission(submissionView.id, verdict);
//
//        int DUPLICATE_SUBMISSIONS = 3;
//        for (int i = 0; i < DUPLICATE_SUBMISSIONS; i++) {
//            SubmissionView duplicateSubmission = submitCodeAndGet(ADMIN_ID, ADMIN_TOKEN, codes1);
//            assertEquals(toViewModel(verdict), duplicateSubmission.getVerdict());
//        }
//
//        // should deploy the judger only once, as other submissions are duplicate and have verdict-shortcuts
//        verify(judgerDeployer, times(1)).deployJudger(any(), anyInt(), any());
//
//        ArgumentCaptor<VerdictIssuedEvent> argumentCaptor = ArgumentCaptor.forClass(VerdictIssuedEvent.class);
//        verify(verdictPublisher, times(DUPLICATE_SUBMISSIONS)).publish(argumentCaptor.capture());
//        argumentCaptor.getAllValues()
//                .forEach(event -> assertEquals(verdict.getBestJudge().getTestcaseName(),
//                        event.getVerdict().getBestJudge().getTestcaseName()));
//
//        // different files --> should respond un-judged submission
//        SubmissionView shouldBeUnJudged = submitCodeAndGet(ADMIN_ID, ADMIN_TOKEN, codes2);
//        assertFalse(shouldBeUnJudged.judged);
//    }

    @Test
    @Timeout(5)
    void GiveSubmitTwoCodesWithAdminTokenAndJudgeFlowHasCompleted_WhenGetBestSubmission_ShouldRespondTheBestOne() throws Exception {
        SubmissionView firstSubmission = submitCodeAndGet(ADMIN_ID, ADMIN_TOKEN);
        SubmissionView secondSubmission = submitCodeAndGet(ADMIN_ID, ADMIN_TOKEN);
        publishVerdictAfterTheWhile(firstSubmission,
                toViewModel(verdict().AC(1, 100, 10).build()));
        shouldNotifyVerdictIssuedEventHandlerWithTimeout();
        publishVerdictAfterTheWhile(secondSubmission,
                toViewModel(verdict().RE(2, 200).build()));
        shouldNotifyVerdictIssuedEventHandlerWithTimeout();

        SubmissionView bestSubmission = getBestSubmission(ADMIN_ID);

        assertEquals(AC, bestSubmission.getVerdict().getSummaryStatus());
    }

    private SubmissionView getBestSubmission(int studentId) throws Exception {
        return getBody(mockMvc.perform(get(API_PREFIX + "/best", problem.getId(), studentId))
                .andExpect(status().isOk()), SubmissionView.class);
    }

    @Test
    void GiveSubmitCodeWithAdminToken_WhenRejudgeThatSubmissionAndGetThatStudentSubmissions_ShouldGetTwoSubmissionWhitTheSameFileId() throws Exception {

        var submission = submitCodeAndGet(ADMIN_ID, ADMIN_TOKEN);

        mockMvc.perform(post("/api/submissions/{submissionId}/judge", submission.getId()))
                .andExpect(status().isOk());

        var afterRejudgeSubmissions = getSubmissionsInPage(ADMIN_ID, ADMIN_TOKEN, 0);
        assertEquals(2, afterRejudgeSubmissions.size());

        var submissions1 = afterRejudgeSubmissions.get(0);
        var submissions2 = afterRejudgeSubmissions.get(1);
        assertEquals(submissions1.submittedCodesFileId, submissions2.submittedCodesFileId);
    }

    @Test
    void GiveSubmit2CodeWithAdmin2Token_WhenRejudgeAllSubmissionsAndRespectivelyGetTwoStudentSubmissions_ShouldGetTwoGroupsSubmissionWhitTheSameFileId() throws Exception {

        var submissionWithCodes1 = submitCodeAndGet(ADMIN_ID, ADMIN_TOKEN, codes1);
        var submissionWithCodes2 = submitCodeAndGet(ADMIN_ID2, ADMIN_TOKEN2, codes2);

        var request = new RejudgeSubmissionsUseCase.Request();
        request.setProblemId(problem.getId());
        mockMvc.perform(post("/api/submissions/judges").param("examId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk());

        var afterRejudgeSubmissions = getSubmissionsInPage(ADMIN_ID, ADMIN_TOKEN, 0);
        assertEquals(2, afterRejudgeSubmissions.size());

        var submissions1 = afterRejudgeSubmissions.get(0);
        var submissions2 = afterRejudgeSubmissions.get(1);
        assertEquals(submissions1.submittedCodesFileId, submissions2.submittedCodesFileId);


        var afterRejudgeSubmissions2 = getSubmissionsInPage(ADMIN_ID2, ADMIN_TOKEN2, 0);
        assertEquals(2, afterRejudgeSubmissions2.size());

        var submissions3 = afterRejudgeSubmissions2.get(0);
        var submissions4 = afterRejudgeSubmissions2.get(1);
        assertEquals(submissions3.submittedCodesFileId, submissions4.submittedCodesFileId);
    }

}
