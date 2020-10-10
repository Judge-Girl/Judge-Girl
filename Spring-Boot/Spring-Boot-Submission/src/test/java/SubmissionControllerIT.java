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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import io.kubernetes.client.apis.BatchV1Api;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.stubs.Stubs;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.entities.submission.SubmissionThrottling;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.springboot.submission.SubmissionServiceApplication;
import tw.waterball.judgegirl.springboot.token.TokenService;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.submissionservice.domain.usecases.SubmitCodeUseCase;
import tw.waterball.judgegirl.submissionservice.ports.SubmissionMessageQueue;
import tw.waterball.judgegirl.testkit.jupiter.ReplaceUnderscoresWithCamelCasesDisplayNameGenerators;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static tw.waterball.judgegirl.springboot.submission.controllers.SubmissionController.SUBMIT_CODE_MULTIPART_KEY_NAME;
import static tw.waterball.judgegirl.testkit.resultmatchers.ZipResultMatcher.zip;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@SpringBootTest
@ActiveProfiles({Profiles.PROD, Profiles.MONGO, Profiles.AMQP})
@AutoConfigureMockMvc
@AutoConfigureDataMongo
@ContextConfiguration(classes = {SubmissionServiceApplication.class, SubmissionControllerIT.TestConfig.class})
@DisplayNameGeneration(ReplaceUnderscoresWithCamelCasesDisplayNameGenerators.class)
public class SubmissionControllerIT {
    private final String API_PREFIX = "/api/problems/{problemId}/students/{studentId}/submissions";
    private final int STUDENT_ID_1 = 1;
    private final Problem problem = Stubs.PROBLEM_TEMPLATE_BUILDER.build();
    private final String SUBMISSION_EXCHANGE_NAME = "submissions";

    @Value("${spring.rabbitmq.username}")
    String amqpUsername;

    @Value("${spring.rabbitmq.password}")
    String amqpPassword;

    @Value("${spring.rabbitmq.virtual-host}")
    String amqpVirtualHost;

    @Value("${spring.rabbitmq.host}")
    String amqpAddress;

    @Value("${spring.rabbitmq.port}")
    int amqpPort;

    @Value("${jwt.test.token-student-id1}")
    String studentId1Token;

    @Value("${jwt.test.token-student-id2}")
    String studentId2Token;

    @Value("${jwt.test.token-admin}")
    String adminToken;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    SubmitCodeUseCase submitCodeUseCase;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SubmissionMessageQueue messageQueue;

    @MockBean
    ProblemServiceDriver problemServiceDriver;

    @MockBean
    BatchV1Api batchV1Api;

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    AmqpTemplate amqpTemplate;

    @Autowired
    TokenService tokenService;

    // For submission
    private MockMultipartFile[] mockFiles = {
            new MockMultipartFile(SUBMIT_CODE_MULTIPART_KEY_NAME, "func1.c", "text/plain",
                    "int plus(int a, int b) {return a + b;}".getBytes()),
            new MockMultipartFile(SUBMIT_CODE_MULTIPART_KEY_NAME, "func2.c", "text/plain",
                    "int minus(int a, int b) {return a - b;}".getBytes())};
    private String ADMIN_TOKEN;

    @BeforeEach
    void setup() throws Exception {
        messageQueue.startListening();
        amqpAdmin.declareExchange(new TopicExchange(SUBMISSION_EXCHANGE_NAME));

        when(problemServiceDriver.getProblem(problem.getId())).thenReturn(
                ProblemView.fromEntity(problem));

        ADMIN_TOKEN = tokenService.createToken(TokenService.Identity.admin()).toString();
    }

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection(Submission.class);
        // throttling must be disabled, otherwise the following submissions will fail (be throttled)
        mongoTemplate.dropCollection(SubmissionThrottling.class);
        messageQueue.stopListening();
    }

    @Test
    void testSubmitAndThenDownload() throws Exception {
        SubmissionView submissionView = givenStudentSubmission(STUDENT_ID_1, studentId1Token);

        // verify get submissions
        mockMvc.perform(get(API_PREFIX, problem.getId(), STUDENT_ID_1)
                .header("Authorization", "bearer " + studentId1Token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(
                        objectMapper.writeValueAsString(singletonList(submissionView))));

        // verify download submitted codes
        mockMvc.perform(get(API_PREFIX + "/{submissionId}/submittedCodes/{submittedCodesFileId}",
                problem.getId(), STUDENT_ID_1, submissionView.id, submissionView.submittedCodesFileId)
                .header("Authorization", "bearer " + studentId1Token))
                .andExpect(status().isOk())
                .andExpect(zip().content(mockFiles));
    }
//
//    @Test
//    void WhenSubmitCodeWithValidToken_ShouldSaveIt_DeployJudger_AndListenAndHandleJudgeResponseFromMQ() throws Exception {
//        Submission submission = givenStudentSubmission(1, studentId1Token);
//        ArgumentCaptor<V1Job> jobCaptor = ArgumentCaptor.forClass(V1Job.class);
//        verify(batchV1Api).createNamespacedJob(anyString(), jobCaptor.capture(), any(), any(), any());
//        V1Job job = jobCaptor.getValue();
//
//        // Verify the applied k8s job is correct according to the problem, submission and the student
//        assertEquals(1, job.getSpec().getTemplate().getSpec().getContainers().size(), "Should only deploy one containers.");
//        V1Container judgerContainer = job.getSpec().getTemplate().getSpec().getContainers().get(0);
//        assertTrue(judgerContainer.getEnv().stream().anyMatch(e ->
//                e.getName().equals("problemId") && Integer.parseInt(e.getValue()) == problem.getId()), "env problemId incorrect.");
//        assertTrue(judgerContainer.getEnv().stream().anyMatch(e ->
//                e.getName().equals("submissionId") && e.getValue().equals(submission.getId())), "env submissionId incorrect.");
//        assertTrue(judgerContainer.getEnv().stream().anyMatch(e ->
//                e.getName().equals("studentId") && Integer.parseInt(e.getValue()) == STUDENT_ID_1), "env studentId incorrect.");
//        V1ResourceRequirements resources = judgerContainer.getResources();
//        assertEquals(problem.getJudgeSpec().getCpu(),
//                resources.getRequests().get("cpu").getNumber().floatValue());
//        assertEquals(problem.getJudgeSpec().getGpu(),
//                resources.getLimits().get("nvidia.com/gpu").getNumber().floatValue());
//
//        // Publish the judge response through message queue after three seconds
//        Delay.delay(3000);
//        JudgeResponse judgeResponse =
//                JudgeResponse.builder()
//                        .problemId(problem.getId())
//                        .problemTitle(problem.getTitle())
//                        .submissionId(submission.getId())
//                        .judge(new Judge("t1", JudgeStatus.AC, 5, 5, "", 20))
//                        .judge(new Judge("t2", JudgeStatus.AC, 6, 6, "", 30))
//                        .judge(new Judge("t3", JudgeStatus.WA, 7, 7, "", 0))
//                        .build();
//        amqpTemplate.convertAndSend(SUBMISSION_EXCHANGE_NAME,
//                String.format("%s.%s.judge", SUBMISSION_EXCHANGE_NAME, submission.getId()),
//                objectMapper.writeValueAsString(judgeResponse));
//
//        // Verify the submission is updated with the judge response
//        submitCodeUseCase.waitForNextJudgeCompletedEventLock.doWait();
//        Submission updatedSubmission = mongoTemplate.findById(submission.getId(), Submission.class);
//        assertNotNull(updatedSubmission);
//        assertEquals(50, updatedSubmission.getTotalGrade());
//        assertEquals(JudgeStatus.WA, updatedSubmission.getSummaryStatus());
//        assertEquals(new HashSet<>(judgeResponse.getJudges()), new HashSet<>(updatedSubmission.getJudges()));
//    }
//
//    @Test
//    void GivenOneSubmissionSucceeded_WhenGetThatSubmissionAsAdmin_ShouldReturn() throws Exception {
//        Submission submission = givenStudentSubmission(1, studentId1Token);
//
//        // verify get submissions
//        mockMvc.perform(get(API_PREFIX, problem.getId(), STUDENT_ID_1)
//                .header("Authorization", "bearer " + adminToken))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(content().json(
//                        objectMapper.writeValueAsString(Collections.singletonList(submission))));
//
//        // verify download submitted codes
//        mockMvc.perform(get(API_PREFIX + "/{submissionId}/zippedSubmittedCodes",
//                problem.getId(), STUDENT_ID_1, submission.getId())
//                .header("Authorization", "bearer " + adminToken))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType("application/zip"))
//                .andExpect(new ZipResultMatcher(mockFiles));
//    }
//
//    @Test
//    void WhenGetOtherStudentSubmissionsWithOwnToken_ShouldBeForbidden() throws Exception {
//        mockMvc.perform(get(API_PREFIX, problem.getId(), 1)
//                .header("Authorization", "bearer " + studentId2Token))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    void GivenOtherStudentSubmissionSucceeded_WhenGetThatSubmissionByIdWithOwnToken_ShouldBeForbidden() throws Exception {
//        Submission submission = givenStudentSubmission(1, studentId1Token);
//        mockMvc.perform(get(API_PREFIX + "/{submissionId}", problem.getId(), 1, submission.getId())
//                .header("Authorization", "bearer " + studentId2Token))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    void GivenOtherStudentSubmissionSucceeded_WhenGetThatSubmissionByIdUnderOwnStudentId_ShouldBeNotFound() throws Exception {
//        Submission submission = givenStudentSubmission(1, studentId1Token);
//        mockMvc.perform(get(API_PREFIX + "/{submissionId}", problem.getId(), 2, submission.getId())
//                .header("Authorization", "bearer " + studentId2Token))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void GivenOtherStudentSubmissionSucceeded_WhenDownloadZippedSubmittedCodesOfThatSubmissionUnderOwnStudentId_ShouldNotFound() throws Exception {
//        Submission submission = givenStudentSubmission(1, studentId1Token);
//
//        mockMvc.perform(get(API_PREFIX + "/zippedSubmittedCodes",
//                problem.getId(), 2, submission.getId())
//                .header("Authorization", "bearer " + studentId2Token))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void GivenOtherStudentSubmissionSucceeded_WhenDownloadZippedSubmittedCodesOfThatSubmissionWithOwnToken_ShouldBeForbidden() throws Exception {
//        Submission submission = givenStudentSubmission(1, studentId1Token);
//
//        mockMvc.perform(get(API_PREFIX + "/zippedSubmittedCodes",
//                problem.getId(), 1, submission.getId())
//                .header("Authorization", "bearer " + studentId2Token))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    void GivenSubmissionsSucceeded_WhenGetThoseSubmissionsInPage_ShouldReturnOnlyThoseSubmissionsInThatPage() throws Exception {
//        List<Submission> submissions = givenParallelStudentSubmissions(1, ADMIN_TOKEN, 5);
//
//        Set<Submission> actualSubmissionsInPreviousPage = new HashSet<>();
//        List<Submission> actualSubmissions;
//        List<Submission> actualAllSubmissions = new ArrayList<>(00);
//
//        int page = 0;
//        do {
//            actualSubmissions = getSubmissionsInPage(page++);
//            assertTrue(actualSubmissions.stream().noneMatch(actualSubmissionsInPreviousPage::contains));
//            actualAllSubmissions.addAll(actualSubmissions);
//            actualSubmissionsInPreviousPage = new HashSet<>(actualSubmissions);
//        } while (!actualSubmissions.isEmpty());
//
//        assertEquals(new HashSet<>(submissions), new HashSet<>(actualAllSubmissions));
//    }
//
//    @Test
//    void WhenSubmitCodeWithOtherStudentToken_ShouldBeForbidden() throws Exception {
//        mockMvc.perform(multipart(API_PREFIX, problem.getId(), 2)
//                .file(mockFiles[0])
//                .file(mockFiles[1])
//                .header("Authorization", "bearer " + studentId1Token))
//                .andExpect(status().isForbidden());
//    }
//
//    private List<Submission> givenParallelStudentSubmissions(int studentId, String token, int count) throws Exception {
//        return IntStream.range(0, count).parallel()
//                .mapToObj(i -> {
//                    try {
//                        return givenStudentSubmission(studentId, token);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }).collect(Collectors.toList());
//    }

    private SubmissionView givenStudentSubmission(int studentId, String token) throws Exception {
        String responseJson = mockMvc.perform(
                multipart(API_PREFIX, problem.getId(), STUDENT_ID_1)
                    .file(mockFiles[0])
                    .file(mockFiles[1])
                .header("Authorization", "bearer " + token))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("studentId").value(studentId))
                .andExpect(jsonPath("problemId").value(problem.getId()))
                .andExpect(jsonPath("submittedCodesFileId").exists())
                .andExpect(jsonPath("submissionTime").exists())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(responseJson, SubmissionView.class);
    }

    public List<Submission> getSubmissionsInPage(int page) throws Exception {
        MvcResult result = mockMvc.perform(get(API_PREFIX + "?page={page}", problem.getId(), STUDENT_ID_1, page)
                .header("Authorization", "bearer " + studentId1Token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<Submission>>() {
                });
    }

    @Configuration
    public static class TestConfig {
        @Bean
        @Primary
        public ConnectionFactory mockRabbitMqConnectionFactory() {
            return new CachingConnectionFactory(new MockConnectionFactory());
        }
    }

}
