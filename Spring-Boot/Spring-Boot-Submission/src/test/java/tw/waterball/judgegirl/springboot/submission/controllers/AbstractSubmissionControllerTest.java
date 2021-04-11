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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.entities.problem.Language;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.stubs.ProblemStubs;
import tw.waterball.judgegirl.entities.submission.Bag;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.entities.submission.SubmissionThrottling;
import tw.waterball.judgegirl.entities.submission.report.Report;
import tw.waterball.judgegirl.entities.submission.verdict.VerdictIssuedEvent;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.springboot.submission.SpringBootSubmissionApplication;
import tw.waterball.judgegirl.springboot.submission.handler.VerdictIssuedEventHandler;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.data.SubmissionData;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.data.VerdictData;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.strategy.SaveSubmissionWithCodesStrategy;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.strategy.VerdictShortcut;
import tw.waterball.judgegirl.submissionapi.clients.VerdictPublisher;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.submissionapi.views.VerdictView;
import tw.waterball.judgegirl.submissionservice.deployer.JudgerDeployer;
import tw.waterball.judgegirl.submissionservice.domain.repositories.SubmissionRepository;
import tw.waterball.judgegirl.submissionservice.domain.usecases.SubmitCodeUseCase;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;
import tw.waterball.judgegirl.testkit.semantics.Spec;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static tw.waterball.judgegirl.commons.token.TokenService.Identity.admin;
import static tw.waterball.judgegirl.commons.token.TokenService.Identity.student;
import static tw.waterball.judgegirl.commons.utils.Delay.delay;
import static tw.waterball.judgegirl.problemapi.views.ProblemView.toViewModel;
import static tw.waterball.judgegirl.submissionapi.clients.SubmissionApiClient.HEADER_BAG_KEY_PREFIX;
import static tw.waterball.judgegirl.submissionapi.clients.SubmissionApiClient.SUBMIT_CODE_MULTIPART_KEY_NAME;
import static tw.waterball.judgegirl.submissionapi.views.SubmissionView.toViewModel;
import static tw.waterball.judgegirl.submissionapi.views.VerdictView.toEntity;
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
    protected final String API_PREFIX = "/api/problems/{problemId}/" + Language.C + "/students/{studentId}/submissions";
    protected final Problem problem = ProblemStubs.problemTemplate().build();
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
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    SubmitCodeUseCase submitCodeUseCase;

    @SpyBean
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
    protected final MockMultipartFile[] codes1 = {
            new MockMultipartFile(SUBMIT_CODE_MULTIPART_KEY_NAME, "func1.c", "text/plain",
                    "int plus(int a, int b) {return a + b;}".getBytes()),
            new MockMultipartFile(SUBMIT_CODE_MULTIPART_KEY_NAME, "func2.c", "text/plain",
                    "int minus(int a, int b) {return a - b;}".getBytes())};
    protected final MockMultipartFile[] codes2 = {
            new MockMultipartFile(SUBMIT_CODE_MULTIPART_KEY_NAME, "func1.c", "text/plain",
                    "int plus(int a, int b) { /*different content*/ return a + b;}".getBytes()),
            new MockMultipartFile(SUBMIT_CODE_MULTIPART_KEY_NAME, "func2.c", "text/plain",
                    "int minus(int a, int b) {return a - b; /*different content*/}".getBytes())};

    protected final Report stubReport = ProblemStubs.compositeReport();

    protected final Bag submissionBag = new Bag() {{
        put("int", "1");
        put("long", "1");
        put("string", "h");
    }};


    @Configuration
    public static class TestConfig {
        @Bean
        @Primary
        public ConnectionFactory mockRabbitMqConnectionFactory() {
            return new CachingConnectionFactory(new MockConnectionFactory());
        }

        @Bean
        @Primary
        public SaveSubmissionWithCodesStrategy useVerdictShortcut(MongoTemplate mongoTemplate, GridFsTemplate gridFsTemplate) {
            return new VerdictShortcut(mongoTemplate, gridFsTemplate);
        }

    }

    @BeforeEach
    void setup() {
        ADMIN_TOKEN = tokenService.createToken(admin(AbstractSubmissionControllerTest.ADMIN_ID)).toString();
        amqpAdmin.declareExchange(new TopicExchange(SUBMISSION_EXCHANGE_NAME));
        STUDENT1_TOKEN = tokenService.createToken(student(AbstractSubmissionControllerTest.STUDENT1_ID)).toString();
        STUDENT2_TOKEN = tokenService.createToken(student(AbstractSubmissionControllerTest.STUDENT2_ID)).toString();
        mockGetProblemById();
    }

    private void mockGetProblemById() {
        when(problemServiceDriver.getProblem(problem.getId())).thenReturn(
                toViewModel(problem));
    }

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection(Submission.class);
        // throttling must be cleared, otherwise the following submissions will fail (be throttled)
        mongoTemplate.dropCollection(SubmissionThrottling.class);
    }

    @SafeVarargs
    protected final VerdictIssuedEvent shouldCompleteJudgeFlow(SubmissionView submission,
                                                               VerdictView verdict,
                                                               JudgeStatus judgeStatus,
                                                               Spec<Submission>... specs) {
        shouldDeployJudger(submission, specs);

        VerdictIssuedEvent verdictIssuedEvent = publishVerdictAfterTheWhile(submission, verdict);

        shouldNotifyVerdictIssuedEventHandler();
        verdictShouldHaveBeenSavedCorrectly(submission, judgeStatus, verdictIssuedEvent);
        return verdictIssuedEvent;
    }

    protected void shouldDeployJudger(SubmissionView submissionView, Spec<Submission>[] specs) {
        ArgumentCaptor<Problem> problemArgumentCaptor = ArgumentCaptor.forClass(Problem.class);
        ArgumentCaptor<Integer> studentIdArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Submission> submissionArgumentCaptor = ArgumentCaptor.forClass(Submission.class);

        verify(judgerDeployer).deployJudger(problemArgumentCaptor.capture(), studentIdArgumentCaptor.capture(),
                submissionArgumentCaptor.capture());
        Submission actualSubmission = submissionArgumentCaptor.getValue();
        assertEquals(submissionView.studentId, studentIdArgumentCaptor.getValue());
        assertEquals(toViewModel(problem), toViewModel(problemArgumentCaptor.getValue()));
        assertEquals(submissionView, toViewModel(actualSubmission));
        Arrays.stream(specs).forEach(s -> s.verify(actualSubmission));
    }

    protected Spec<Submission> shouldBringSubmissionBagToJudger() {
        return submission -> assertEquals(submissionBag, submission.getBag());
    }

    protected VerdictIssuedEvent publishVerdictAfterTheWhile(SubmissionView submissionView, VerdictView verdict) {
        delay(3000);
        VerdictIssuedEvent verdictIssuedEvent = generateVerdictIssuedEvent(submissionView, verdict);
        verdictPublisher.publish(verdictIssuedEvent);
        return verdictIssuedEvent;
    }

    protected void shouldNotifyVerdictIssuedEventHandler() {
        verdictIssuedEventHandler.onHandlingCompletion$.doWait(5000);
    }

    protected void verdictShouldHaveBeenSavedCorrectly(SubmissionView submissionView,
                                                       JudgeStatus judgeStatus,
                                                       VerdictIssuedEvent verdictIssuedEvent) {
        SubmissionData updatedSubmissionData = mongoTemplate.findById(submissionView.getId(), SubmissionData.class);
        assertNotNull(updatedSubmissionData);
        VerdictData verdictData = updatedSubmissionData.getVerdict();
        assertEquals(50, verdictData.getTotalGrade());
        assertEquals(judgeStatus, verdictData.getSummaryStatus());
        assertEquals(new HashSet<>(verdictIssuedEvent.getVerdict().getJudges()),
                new HashSet<>(verdictData.getJudges()));
        assertEquals(stubReport.getRawData(), verdictData.getReportData());
    }

    protected VerdictIssuedEvent generateVerdictIssuedEvent(SubmissionView submissionView, VerdictView verdict) {
        return VerdictIssuedEvent.builder()
                .problemId(problem.getId())
                .studentId(submissionView.studentId)
                .problemTitle(problem.getTitle())
                .submissionId(submissionView.getId())
                .verdict(toEntity(verdict)).build();
    }


    protected List<SubmissionView> givenParallelStudentSubmissions(int studentId, int count) {
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
                .andExpect(zip().content(codes1));
    }


    protected void givenSubmission(Submission submission) {
        submissionRepository.save(submission);
    }

    protected SubmissionView submitCodeAndGet(int studentId, String token) throws Exception {
        return submitCodeAndGet(studentId, token, codes1);
    }

    protected SubmissionView submitCodeAndGet(int studentId, String token, MockMultipartFile... files) throws Exception {
        return getBody(submitCode(studentId, token, files)
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("studentId").value(studentId))
                .andExpect(jsonPath("problemId").value(problem.getId()))
                .andExpect(jsonPath("submittedCodesFileId").exists())
                .andExpect(jsonPath("submissionTime").exists()), SubmissionView.class);
    }

    protected ResultActions submitCode(int studentId, String token, MockMultipartFile... files) throws Exception {
        return requestWithToken(() ->
                multipartRequestWithSubmittedCodes(studentId, files), token);
    }

    protected MockHttpServletRequestBuilder multipartRequestWithSubmittedCodes(int studentId, MockMultipartFile... files) {
        var call = multipart(API_PREFIX, problem.getId(), studentId);
        for (MockMultipartFile file : files) {
            call = call.file(file);
        }
        MockHttpServletRequestBuilder addingHeaders = call;
        for (var entry : submissionBag.entrySet()) {
            addingHeaders = call.header(HEADER_BAG_KEY_PREFIX + entry.getKey(), entry.getValue());
        }
        return addingHeaders;
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

}