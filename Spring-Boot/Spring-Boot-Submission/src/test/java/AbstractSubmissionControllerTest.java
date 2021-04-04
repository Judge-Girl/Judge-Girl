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

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.entities.problem.Language;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.stubs.ProblemStubs;
import tw.waterball.judgegirl.entities.submission.*;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.springboot.submission.SpringBootSubmissionApplication;
import tw.waterball.judgegirl.springboot.submission.controllers.VerdictIssuedEventHandler;
import tw.waterball.judgegirl.submissionapi.clients.VerdictPublisher;
import tw.waterball.judgegirl.submissionapi.views.ReportView;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.submissionapi.views.VerdictIssuedEvent;
import tw.waterball.judgegirl.submissionapi.views.VerdictView;
import tw.waterball.judgegirl.submissionservice.deployer.JudgerDeployer;
import tw.waterball.judgegirl.submissionservice.domain.repositories.SubmissionRepository;
import tw.waterball.judgegirl.submissionservice.domain.usecases.SubmitCodeUseCase;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static tw.waterball.judgegirl.commons.token.TokenService.Identity.admin;
import static tw.waterball.judgegirl.commons.token.TokenService.Identity.student;
import static tw.waterball.judgegirl.submissionapi.clients.SubmissionApiClient.SUBMIT_CODE_MULTIPART_KEY_NAME;
import static tw.waterball.judgegirl.testkit.resultmatchers.ZipResultMatcher.zip;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@ActiveProfiles({Profiles.JWT, Profiles.EMBEDDED_MONGO, Profiles.AMQP, Profiles.K8S})
@AutoConfigureMockMvc
@ContextConfiguration(classes = {SpringBootSubmissionApplication.class, AbstractSubmissionControllerTest.TestConfig.class})
public class AbstractSubmissionControllerTest extends AbstractSpringBootTest {
    public static final int ADMIN_ID = 12345;
    public static final int STUDENT1_ID = 22;
    public static final int STUDENT2_ID = 34;
    protected final String API_PREFIX = "/api/problems/{problemId}/" + Language.C.toString() + "/students/{studentId}/submissions";
    protected final Problem problem = ProblemStubs.template().build();
    protected final String SUBMISSION_EXCHANGE_NAME = "submissions";
    protected String ADMIN_TOKEN;
    protected String STUDENT1_TOKEN;
    protected String STUDENT2_TOKEN;

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

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    SubmitCodeUseCase submitCodeUseCase;

    @Autowired
    VerdictPublisher verdictPublisher;

    @MockBean
    ProblemServiceDriver problemServiceDriver;

    @MockBean
    JudgerDeployer judgerDeployer;

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    AmqpTemplate amqpTemplate;

    @Autowired
    TokenService tokenService;

    @Autowired
    VerdictIssuedEventHandler verdictIssuedEventHandler;

    @Autowired
    SubmissionRepository submissionRepository;

    // For submission
    protected final MockMultipartFile[] mockFiles = {
            new MockMultipartFile(SUBMIT_CODE_MULTIPART_KEY_NAME, "func1.c", "text/plain",
                    "int plus(int a, int b) {return a + b;}".getBytes()),
            new MockMultipartFile(SUBMIT_CODE_MULTIPART_KEY_NAME, "func2.c", "text/plain",
                    "int minus(int a, int b) {return a - b;}".getBytes())};

    protected final Report stubReport = ProblemStubs.compositeReport();

    @BeforeEach
    void setup() {
        ADMIN_TOKEN = tokenService.createToken(admin(ADMIN_ID)).toString();
        amqpAdmin.declareExchange(new TopicExchange(SUBMISSION_EXCHANGE_NAME));
        STUDENT1_TOKEN = tokenService.createToken(student(STUDENT1_ID)).toString();
        STUDENT2_TOKEN = tokenService.createToken(student(STUDENT2_ID)).toString();
        mockGetProblemById();
    }

    private void mockGetProblemById() {
        when(problemServiceDriver.getProblem(problem.getId())).thenReturn(
                ProblemView.fromEntity(problem));
    }

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection(Submission.class);
        // throttling must be disabled, otherwise the following submissions will fail (be throttled)
        mongoTemplate.dropCollection(SubmissionThrottling.class);
    }

    protected void verifyJudgerDeployed(SubmissionView submissionView) {
        ArgumentCaptor<Problem> problemArgumentCaptor = ArgumentCaptor.forClass(Problem.class);
        ArgumentCaptor<Integer> studentIdArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Submission> submissionArgumentCaptor = ArgumentCaptor.forClass(Submission.class);

        verify(judgerDeployer).deployJudger(problemArgumentCaptor.capture(), studentIdArgumentCaptor.capture(),
                submissionArgumentCaptor.capture());
        assertEquals(STUDENT1_ID, studentIdArgumentCaptor.getValue());
        assertEquals(ProblemView.fromEntity(problem), ProblemView.fromEntity(problemArgumentCaptor.getValue()));
        assertEquals(submissionView, SubmissionView.fromEntity(submissionArgumentCaptor.getValue()));
    }

    protected VerdictIssuedEvent generateVerdictIssuedEvent(SubmissionView submissionView) {
        return VerdictIssuedEvent.builder()
                .problemId(problem.getId())
                .studentId(submissionView.studentId)
                .problemTitle(problem.getTitle())
                .submissionId(submissionView.getId())
                .verdict(VerdictView.builder()
                        .judge(new Judge("t1", JudgeStatus.AC, new ProgramProfile(5, 5, ""), 20))
                        .judge(new Judge("t2", JudgeStatus.AC, new ProgramProfile(6, 6, ""), 30))
                        .judge(new Judge("t3", JudgeStatus.WA, new ProgramProfile(7, 7, ""), 0))
                        .issueTime(new Date())
                        .report(ReportView.fromEntity(ProblemStubs.compositeReport()))
                        .build()).build();
    }


    protected List<SubmissionView> givenParallelStudentSubmissions(int studentId, int count) throws Exception {
        return IntStream.range(0, count).parallel()
                .mapToObj(i -> {
                    try {
                        return submitCodeAndGet(studentId,
                                /*Must use the Admin token to avoid SubmissionThrottling*/
                                ADMIN_TOKEN);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

    }


    protected ResultActions requestGetSubmission(int studentId, String studentToken) throws Exception {
        return requestWithToken(() -> get(API_PREFIX, problem.getId(), studentId), studentToken)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    protected ResultActions requestDownloadSubmittedCodes(int studentId, String studentToken, String submissionId, String submittedCodesFile) throws Exception {
        return requestWithToken(() -> get(API_PREFIX + "/{submissionId}/submittedCodes/{submittedCodesFileId}",
                problem.getId(), studentId, submissionId, submittedCodesFile), studentToken)
                .andExpect(status().isOk())
                .andExpect(zip().content(mockFiles));
    }

    protected void givenSubmission(Submission submission) {
        submissionRepository.save(submission);
    }

    protected SubmissionView submitCodeAndGet(int studentId, String token) throws Exception {
        String responseJson = submitCode(studentId, token)
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

    protected ResultActions submitCode(int studentId, String token) throws Exception {
        return requestWithToken(() ->
                multipartRequestWithSubmittedCodes(studentId), token);
    }

    protected MockMultipartHttpServletRequestBuilder multipartRequestWithSubmittedCodes(int studentId) {
        return multipart(API_PREFIX, problem.getId(), studentId)
                .file(mockFiles[0])
                .file(mockFiles[1]);
    }

    protected List<SubmissionView> getSubmissionsInPage(int studentId, String studentToken, int page) throws Exception {
        MvcResult result = requestWithToken(() -> get(API_PREFIX + "?page={page}",
                problem.getId(), studentId, page), studentToken).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
    }

    protected ResultActions requestWithToken(Supplier<MockHttpServletRequestBuilder> requestBuilderSupplier,
                                             String token) throws Exception {
        return mockMvc.perform(requestBuilderSupplier.get()
                .header("Authorization", "bearer " + token));
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
