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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.MultiValueMap;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.commons.token.TokenService.Token;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.stubs.ProblemStubs;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.primitives.submission.SubmissionThrottling;
import tw.waterball.judgegirl.primitives.submission.report.Report;
import tw.waterball.judgegirl.primitives.submission.verdict.VerdictIssuedEvent;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.springboot.submission.SpringBootSubmissionApplication;
import tw.waterball.judgegirl.springboot.submission.handler.VerdictIssuedEventHandler;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.data.DataMapper;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.data.SubmissionData;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.data.VerdictData;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.strategy.SaveSubmissionWithCodesStrategy;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.strategy.VerdictShortcut;
import tw.waterball.judgegirl.submission.deployer.JudgerDeployer;
import tw.waterball.judgegirl.submission.domain.repositories.SubmissionRepository;
import tw.waterball.judgegirl.submission.domain.usecases.SubmitCodeUseCase;
import tw.waterball.judgegirl.submissionapi.clients.VerdictPublisher;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.submissionapi.views.VerdictView;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;
import tw.waterball.judgegirl.testkit.semantics.Spec;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Optional.of;
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
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;
import static tw.waterball.judgegirl.primitives.time.DateProvider.now;
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
@ContextConfiguration(classes = {SpringBootSubmissionApplication.class, AbstractSubmissionControllerTest.TestConfig.class})
public class AbstractSubmissionControllerTest extends AbstractSpringBootTest {
    public static final int ADMIN_ID = 12345;
    public static final int STUDENT1_ID = 22;
    public static final int STUDENT2_ID = 34;
    protected final String API_PREFIX = "/api/problems/{problemId}/" + Language.C + "/students/{studentId}/submissions";
    protected final Problem problem = ProblemStubs.problemTemplate().build();
    protected final String SUBMISSION_EXCHANGE_NAME = "submissions";
    protected Token ADMIN_TOKEN;
    protected Token STUDENT1_TOKEN;
    protected Token STUDENT2_TOKEN;

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
                    "int plus(int a, int b) {return a + b;}" .getBytes()),
            new MockMultipartFile(SUBMIT_CODE_MULTIPART_KEY_NAME, "func2.c", "text/plain",
                    "int minus(int a, int b) {return a - b;}" .getBytes())};
    protected final MockMultipartFile[] codes2 = {
            new MockMultipartFile(SUBMIT_CODE_MULTIPART_KEY_NAME, "func1.c", "text/plain",
                    "int plus(int a, int b) { /*different content*/ return a + b;}" .getBytes()),
            new MockMultipartFile(SUBMIT_CODE_MULTIPART_KEY_NAME, "func2.c", "text/plain",
                    "int minus(int a, int b) {return a - b; /*different content*/}" .getBytes())};

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
        ADMIN_TOKEN = tokenService.createToken(admin(ADMIN_ID));
        amqpAdmin.declareExchange(new TopicExchange(SUBMISSION_EXCHANGE_NAME));
        STUDENT1_TOKEN = tokenService.createToken(student(STUDENT1_ID));
        STUDENT2_TOKEN = tokenService.createToken(student(STUDENT2_ID));
        mockGetProblemById();
    }

    private void mockGetProblemById() {
        when(problemServiceDriver.getProblem(problem.getId()))
                .thenReturn(of(problem).map(ProblemView::toViewModel));
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
                                                               int expectTotalGrade,
                                                               Spec<Submission>... specs) {
        shouldDeployJudger(submission, specs);

        VerdictIssuedEvent verdictIssuedEvent = publishVerdictAfterTheWhile(submission, verdict);

        shouldNotifyVerdictIssuedEventHandlerWithTimeout(5000);
        verdictShouldHaveBeenSavedCorrectly(submission, judgeStatus, expectTotalGrade, verdictIssuedEvent);
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
        delay(1000);
        VerdictIssuedEvent verdictIssuedEvent = generateVerdictIssuedEvent(submissionView, verdict);
        verdictPublisher.publish(verdictIssuedEvent);
        return verdictIssuedEvent;
    }

    protected void shouldNotifyVerdictIssuedEventHandlerWithTimeout(long timeout) {
        verdictIssuedEventHandler.onHandlingCompletion$.doWait(timeout);
    }

    protected void verdictShouldHaveBeenSavedCorrectly(SubmissionView submissionView,
                                                       JudgeStatus judgeStatus,
                                                       int expectTotalGrade,
                                                       VerdictIssuedEvent verdictIssuedEvent) {
        SubmissionData updatedSubmissionData = mongoTemplate.findById(submissionView.getId(), SubmissionData.class);
        assertNotNull(updatedSubmissionData);
        VerdictData verdictData = updatedSubmissionData.getVerdict();
        assertEquals(expectTotalGrade, verdictData.getTotalGrade());
        assertEquals(judgeStatus, verdictData.getSummaryStatus());
        assertEquals(new HashSet<>(verdictIssuedEvent.getVerdict().getJudges()),
                new HashSet<>(mapToList(verdictData.getJudges(), DataMapper::toEntity)));
        assertEquals(stubReport.getRawData(), verdictData.getReportData());
    }

    protected VerdictIssuedEvent generateVerdictIssuedEvent(SubmissionView submissionView, VerdictView verdict) {
        return VerdictIssuedEvent.builder()
                .problemId(problem.getId())
                .studentId(submissionView.studentId)
                .problemTitle(problem.getTitle())
                .submissionId(submissionView.getId())
                .submissionTime(now())
                .verdict(toEntity(verdict)).build();
    }


    protected List<SubmissionView> givenStudentSubmissionsSubmittedConcurrently(int studentId, int count) {
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

    protected void submissionsShouldHaveIds(List<SubmissionView> submissions, String... ids) {
        for (int i = 0; i < submissions.size(); i++) {
            assertEquals(ids[i], submissions.get(i).getId());
        }
    }

    protected ResultActions requestGetSubmission(int studentId, Token studentToken) throws Exception {
        return mockMvc.perform(withToken(studentToken,
                get(API_PREFIX, problem.getId(), studentId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    protected ResultActions requestDownloadSubmittedCodes(int studentId, Token studentToken, String submissionId, String submittedCodesFile) throws Exception {
        return mockMvc.perform(withToken(studentToken,
                get(API_PREFIX + "/{submissionId}/submittedCodes/{submittedCodesFileId}",
                        problem.getId(), studentId, submissionId, submittedCodesFile)))
                .andExpect(status().isOk())
                .andExpect(zip().content(codes1));
    }

    protected void givenSubmission(Submission submission) {
        submissionRepository.save(submission);
    }

    protected SubmissionView submitCodeAndGet(int studentId, Token token) throws Exception {
        return submitCodeAndGet(studentId, token, codes1);
    }

    protected SubmissionView submitCodeAndGet(int studentId, Token token, MockMultipartFile... files) throws Exception {
        return getBody(submitCode(studentId, token, files)
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("studentId").value(studentId))
                .andExpect(jsonPath("problemId").value(problem.getId()))
                .andExpect(jsonPath("submittedCodesFileId").exists())
                .andExpect(jsonPath("submissionTime").exists()), SubmissionView.class);
    }

    protected ResultActions submitCode(int studentId, Token token, MockMultipartFile... files) throws Exception {
        return mockMvc.perform(withToken(token,
                multipartRequestWithSubmittedCodes(studentId, files)));
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

    protected List<SubmissionView> getSubmissionsWithBagQuery(int studentId, Token studentToken, MultiValueMap<String, String> bagQueryParameters) throws Exception {
        return getBody(mockMvc.perform(withToken(studentToken,
                get(API_PREFIX, problem.getId(), studentId).queryParams(bagQueryParameters)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)), new TypeReference<>() {
        });
    }

    protected List<SubmissionView> getSubmissionsInPage(int studentId, Token studentToken, int page) throws Exception {
        return getBody(mockMvc.perform(withToken(studentToken,
                get(API_PREFIX, problem.getId(), studentId)
                        .queryParam("page", String.valueOf(page))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)), new TypeReference<>() {
        });
    }

}
