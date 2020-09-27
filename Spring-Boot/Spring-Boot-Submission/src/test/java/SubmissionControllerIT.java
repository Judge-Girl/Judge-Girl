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
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1Job;
import io.kubernetes.client.models.V1ResourceRequirements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import tw.waterball.judgegirl.entities.submission.Judge;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.commons.profiles.Profiles;
import tw.waterball.judgegirl.entities.submission.JudgeResponse;
import tw.waterball.judgegirl.submissionservice.ports.SubmissionMessageQueue;
import tw.waterball.judgegirl.commons.services.token.TokenService;
import tw.waterball.judgegirl.commons.utils.Delay;
import tw.waterball.judgegirl.problemapi.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.entities.submission.SubmissionThrottling;
import tw.waterball.judgegirl.submissionservice.ports.JudgerDeployer;
import tw.waterball.judgegirl.testkit.jupiter.ReplaceUnderscoresAndCamelCaseDisplayNameGenerator;
import tw.waterball.judgegirl.testkit.zip.ZipResultMatcher;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static tw.waterball.judgegirl.commons.utils.Stubs.PROBLEM_STUB;
import static tw.waterball.judgegirl.springboot.submission.controllers.SubmissionController.SUBMIT_CODE_MULTIPART_KEY_NAME;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@SpringBootTest
@ActiveProfiles({Profiles.PROD, Profiles.MONGO, Profiles.AMQP, Profiles.K8S})
@AutoConfigureMockMvc
@AutoConfigureDataMongo
@ContextConfiguration(classes = {SubmissionServiceApplication.class, SubmissionControllerIT.TestConfig.class})
@DisplayNameGeneration(ReplaceUnderscoresAndCamelCaseDisplayNameGenerator.class)
public class SubmissionControllerIT {
    final String API_PREFIX = "/api/problems/{problemId}/students/{studentId}/submissions";
    private final int STUDENT_ID_1 = 1;
    private final Problem PROBLEM = PROBLEM_STUB;
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
    SubmissionServiceImpl submissionService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SubmissionMessageQueue messageQueue;

    @MockBean
    ProblemServiceDriver problemServiceDriver;

    @Autowired
    JudgerDeployer judgerDeployer;

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

        when(problemServiceDriver.getProblem(PROBLEM.getId())).thenReturn(
                ProblemView.fromEntity(PROBLEM_STUB));

        ADMIN_TOKEN = tokenService.createToken(TokenService.Identity.admin()).toString();
    }

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection(Submission.class);
        // throttling must be dropped, otherwise multiple submissions will fail
        mongoTemplate.dropCollection(SubmissionThrottling.class);
        messageQueue.stopListening();
    }

    @Test
    void testSubmitAndThenDownload() throws Exception {
        Submission submission = givenStudentSubmission(1, studentId1Token);

        // verify get submissions
        mockMvc.perform(get(API_PREFIX, PROBLEM.getId(), STUDENT_ID_1)
                .header("Authorization", "Bear " + studentId1Token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(
                        objectMapper.writeValueAsString(Collections.singletonList(submission))));

        // verify download submitted codes
        mockMvc.perform(get(API_PREFIX + "/{submissionId}/zippedSubmittedCodes",
                PROBLEM.getId(), STUDENT_ID_1, submission.getId())
                .header("Authorization", "Bear " + studentId1Token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andExpect(new ZipResultMatcher(mockFiles));
    }

    @Test
    void WhenSubmitCodeWithValidToken_ShouldSaveIt_DeployJudger_AndListenAndHandleJudgeResponseFromMQ() throws Exception {
        Submission submission = givenStudentSubmission(1, studentId1Token);
        ArgumentCaptor<V1Job> jobCaptor = ArgumentCaptor.forClass(V1Job.class);
        verify(batchV1Api).createNamespacedJob(anyString(), jobCaptor.capture(), any(), any(), any());
        V1Job job = jobCaptor.getValue();

        // Verify the applied k8s job is correct according to the problem, submission and the student
        assertEquals(1, job.getSpec().getTemplate().getSpec().getContainers().size(), "Should only deploy one containers.");
        V1Container judgerContainer = job.getSpec().getTemplate().getSpec().getContainers().get(0);
        assertTrue(judgerContainer.getEnv().stream().anyMatch(e ->
                e.getName().equals("problemId") && Integer.parseInt(e.getValue()) == PROBLEM.getId()), "env problemId incorrect.");
        assertTrue(judgerContainer.getEnv().stream().anyMatch(e ->
                e.getName().equals("submissionId") && e.getValue().equals(submission.getId())), "env submissionId incorrect.");
        assertTrue(judgerContainer.getEnv().stream().anyMatch(e ->
                e.getName().equals("studentId") && Integer.parseInt(e.getValue()) == STUDENT_ID_1), "env studentId incorrect.");
        V1ResourceRequirements resources = judgerContainer.getResources();
        assertEquals(PROBLEM.getJudgeSpec().getCpu(),
                resources.getRequests().get("cpu").getNumber().floatValue());
        assertEquals(PROBLEM.getJudgeSpec().getGpu(),
                resources.getLimits().get("nvidia.com/gpu").getNumber().floatValue());

        // Publish the judge response through message queue after three seconds
        Delay.delay(3000);
        JudgeResponse judgeResponse =
                JudgeResponse.builder()
                        .problemId(PROBLEM.getId())
                        .problemTitle(PROBLEM.getTitle())
                        .submissionId(submission.getId())
                        .judge(new Judge("t1", JudgeStatus.AC, 5, 5, "", 20))
                        .judge(new Judge("t2", JudgeStatus.AC, 6, 6, "", 30))
                        .judge(new Judge("t3", JudgeStatus.WA, 7, 7, "", 0))
                        .build();
        amqpTemplate.convertAndSend(SUBMISSION_EXCHANGE_NAME,
                String.format("%s.%s.judge", SUBMISSION_EXCHANGE_NAME, submission.getId()),
                objectMapper.writeValueAsString(judgeResponse));

        // Verify the submission is updated with the judge response
        submissionService.waitForNextJudgeCompletedEventLock.doWait();
        Submission updatedSubmission = mongoTemplate.findById(submission.getId(), Submission.class);
        assertNotNull(updatedSubmission);
        assertEquals(50, updatedSubmission.getTotalGrade());
        assertEquals(JudgeStatus.WA, updatedSubmission.getSummaryStatus());
        assertEquals(new HashSet<>(judgeResponse.getJudges()), new HashSet<>(updatedSubmission.getJudges()));

    }

    @Test
    void GivenOneSubmissionSucceeded_WhenGetThatSubmissionAsAdmin_ShouldReturn() throws Exception {
        Submission submission = givenStudentSubmission(1, studentId1Token);

        // verify get submissions
        mockMvc.perform(get(API_PREFIX, PROBLEM.getId(), STUDENT_ID_1)
                .header("Authorization", "Bear " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(
                        objectMapper.writeValueAsString(Collections.singletonList(submission))));

        // verify download submitted codes
        mockMvc.perform(get(API_PREFIX + "/{submissionId}/zippedSubmittedCodes",
                PROBLEM.getId(), STUDENT_ID_1, submission.getId())
                .header("Authorization", "Bear " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andExpect(new ZipResultMatcher(mockFiles));
    }

    @Test
    void WhenGetOtherStudentSubmissionsWithOwnToken_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get(API_PREFIX, PROBLEM.getId(), 1)
                .header("Authorization", "Bear " + studentId2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void GivenOtherStudentSubmissionSucceeded_WhenGetThatSubmissionByIdWithOwnToken_ShouldBeForbidden() throws Exception {
        Submission submission = givenStudentSubmission(1, studentId1Token);
        mockMvc.perform(get(API_PREFIX + "/{submissionId}", PROBLEM.getId(), 1, submission.getId())
                .header("Authorization", "Bear " + studentId2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void GivenOtherStudentSubmissionSucceeded_WhenGetThatSubmissionByIdUnderOwnStudentId_ShouldBeNotFound() throws Exception {
        Submission submission = givenStudentSubmission(1, studentId1Token);
        mockMvc.perform(get(API_PREFIX + "/{submissionId}", PROBLEM.getId(), 2, submission.getId())
                .header("Authorization", "Bear " + studentId2Token))
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenOtherStudentSubmissionSucceeded_WhenDownloadZippedSubmittedCodesOfThatSubmissionUnderOwnStudentId_ShouldNotFound() throws Exception {
        Submission submission = givenStudentSubmission(1, studentId1Token);

        mockMvc.perform(get(API_PREFIX + "/zippedSubmittedCodes",
                PROBLEM.getId(), 2, submission.getId())
                .header("Authorization", "Bear " + studentId2Token))
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenOtherStudentSubmissionSucceeded_WhenDownloadZippedSubmittedCodesOfThatSubmissionWithOwnToken_ShouldBeForbidden() throws Exception {
        Submission submission = givenStudentSubmission(1, studentId1Token);

        mockMvc.perform(get(API_PREFIX + "/zippedSubmittedCodes",
                PROBLEM.getId(), 1, submission.getId())
                .header("Authorization", "Bear " + studentId2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void GivenSubmissionsSucceeded_WhenGetThoseSubmissionsInPage_ShouldReturnOnlyThoseSubmissionsInThatPage() throws Exception {
        List<Submission> submissions = givenParallelStudentSubmissions(1, ADMIN_TOKEN, 5);

        Set<Submission> actualSubmissionsInPreviousPage = new HashSet<>();
        List<Submission> actualSubmissions;
        List<Submission> actualAllSubmissions = new ArrayList<>(00);

        int page = 0;
        do {
            actualSubmissions = getSubmissionsInPage(page++);
            assertTrue(actualSubmissions.stream().noneMatch(actualSubmissionsInPreviousPage::contains));
            actualAllSubmissions.addAll(actualSubmissions);
            actualSubmissionsInPreviousPage = new HashSet<>(actualSubmissions);
        } while (!actualSubmissions.isEmpty());

        assertEquals(new HashSet<>(submissions), new HashSet<>(actualAllSubmissions));
    }

    @Test
    void WhenSubmitCodeWithOtherStudentToken_ShouldBeForbidden() throws Exception {
        mockMvc.perform(multipart(API_PREFIX, PROBLEM.getId(), 2)
                .file(mockFiles[0])
                .file(mockFiles[1])
                .header("Authorization", "Bear " + studentId1Token))
                .andExpect(status().isForbidden());
    }

    private List<Submission> givenParallelStudentSubmissions(int studentId, String token, int count) throws Exception {
        return IntStream.range(0, count).parallel()
                .mapToObj(i -> {
                    try {
                        return givenStudentSubmission(studentId, token);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
    }

    private Submission givenStudentSubmission(int studentId, String token) throws Exception {
        // perform API request and expect submission responded
        String responseJson = mockMvc.perform(multipart(API_PREFIX, PROBLEM.getId(), STUDENT_ID_1)
                .file(mockFiles[0])
                .file(mockFiles[1])
                .header("Authorization", "Bear " + token))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("studentId").value(studentId))
                .andExpect(jsonPath("problemId").value(PROBLEM.getId()))
                .andExpect(jsonPath("judges").isEmpty())
                .andExpect(jsonPath("zippedSubmittedCodeFilesId").exists())
                .andExpect(jsonPath("submissionTime").exists())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, Submission.class);
    }

    public List<Submission> getSubmissionsInPage(int page) throws Exception {
        MvcResult result = mockMvc.perform(get(API_PREFIX + "?page={page}", PROBLEM.getId(), STUDENT_ID_1, page)
                .header("Authorization", "Bear " + studentId1Token))
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
