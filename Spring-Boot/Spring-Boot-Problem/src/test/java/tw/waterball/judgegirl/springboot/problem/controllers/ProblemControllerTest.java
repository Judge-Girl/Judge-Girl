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
package tw.waterball.judgegirl.springboot.problem.controllers;


import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.commons.token.TokenService.Identity;
import tw.waterball.judgegirl.commons.token.TokenService.Token;
import tw.waterball.judgegirl.commons.utils.ResourceUtils;
import tw.waterball.judgegirl.primitives.problem.JudgePluginTag;
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;
import tw.waterball.judgegirl.problem.domain.usecases.PatchProblemUseCase;
import tw.waterball.judgegirl.problem.domain.usecases.PatchProblemUseCase.LanguageEnvUpsert;
import tw.waterball.judgegirl.problem.domain.usecases.PatchProblemUseCase.TestcaseUpsert;
import tw.waterball.judgegirl.problemapi.views.*;
import tw.waterball.judgegirl.springboot.problem.SpringBootProblemApplication;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.springboot.profiles.productions.Redis;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;
import tw.waterball.judgegirl.testkit.semantics.WithHeader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.*;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static tw.waterball.judgegirl.commons.utils.HttpHeaderUtils.bearerWithToken;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.*;
import static tw.waterball.judgegirl.commons.utils.ZipUtils.zipFilesFromResources;
import static tw.waterball.judgegirl.primitives.problem.JudgePluginTag.Type.OUTPUT_MATCH_POLICY;
import static tw.waterball.judgegirl.primitives.stubs.ProblemStubs.languageEnvTemplate;
import static tw.waterball.judgegirl.primitives.stubs.ProblemStubs.problemTemplate;
import static tw.waterball.judgegirl.problem.domain.usecases.PatchProblemUseCase.TestcaseUpsert.upsert;
import static tw.waterball.judgegirl.problem.domain.usecases.UploadProvidedCodeUseCase.PROVIDED_CODE_MULTIPART_KEY_NAME;
import static tw.waterball.judgegirl.problemapi.views.ProblemItem.toProblemItem;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Testcontainers
@ActiveProfiles({Profiles.JWT, Profiles.EMBEDDED_MONGO, Profiles.REDIS})
@AutoConfigureDataMongo
@ContextConfiguration(classes = {SpringBootProblemApplication.class, ProblemControllerTest.RedisConfig.class})
public class ProblemControllerTest extends AbstractSpringBootTest {

    public static final String REDIS_IMAGE_NAME = "redis";
    public static final int REDIS_PORT = 6379;
    public static final int ADMIN_ID = 12345;
    public static final int STUDENT1_ID = 22;
    public static final String API_PREFIX = "/api/problems";

    @Container
    public static GenericContainer<?> REDIS = new GenericContainer<>(
            DockerImageName.parse(REDIS_IMAGE_NAME)).withExposedPorts(REDIS_PORT);

    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    ProblemRepository problemRepository;
    @Autowired
    TokenService tokenService;

    private byte[] providedCodesZip;
    private byte[] testcaseIOsZip;
    private Token adminToken;
    private Token student1Token;

    @Redis
    @Configuration
    public static class RedisConfig {

        @Bean
        @Primary
        public RedisConnectionFactory testRedisConnectionFactory() {
            String address = REDIS.getHost();
            int port = REDIS.getMappedPort(REDIS_PORT);
            var redisConfig = new RedisStandaloneConfiguration(address, port);
            return new LettuceConnectionFactory(redisConfig);
        }

    }

    @BeforeEach
    void setup() {
        adminToken = tokenService.createToken(Identity.admin(ADMIN_ID));
        student1Token = tokenService.createToken(Identity.student(STUDENT1_ID));
    }

    @AfterEach
    void clean() {
        problemRepository.deleteAll();
    }

    @Test
    void GivenProblemSavedWithProvidedCodesAndTestcaseIOs_WhenDownloadProvidedCodes_ShouldSucceed() throws Exception {
        var problem = givenProblemSavedWithProvidedCodesAndTestcaseIOs();

        LanguageEnv languageEnv = problem.getLanguageEnv(Language.C);

        downloadProvidedCodes(problem.getId(), languageEnv);
    }

    private void downloadProvidedCodes(int problemId, LanguageEnv languageEnv) throws Exception {
        mockMvc.perform(withToken(adminToken,
                get(API_PREFIX + "/{problemId}/{languageEnv}/providedCodes/{providedCodesFileId}",
                        problemId, languageEnv.getName(), languageEnv.getProvidedCodesFileId())))
                .andExpect(status().isOk())
                .andExpect(header().longValue("Content-Length", providedCodesZip.length))
                .andExpect(content().contentType("application/zip"))
                .andExpect(content().bytes(providedCodesZip));
    }

    @Test
    void GivenProblemSaved_WhenGetProblemById_ShouldRespondThatProblem() throws Exception {
        Problem problem = givenOneProblemSaved();

        var actualProblem = getProblem(withToken(adminToken), problem.getId());

        assertProblemEquals(toViewModel(problem), actualProblem);
    }


    @Test
    void GivenProblemSaved_DownloadZippedTestCaseIOsShouldSucceed() throws Exception {
        var problem = givenProblemSavedWithProvidedCodesAndTestcaseIOs();

        downloadTestcaseIOs(problem);
    }

    private void downloadTestcaseIOs(Problem problem) throws Exception {
        mockMvc.perform(withToken(adminToken,
                get(API_PREFIX + "/{problemId}/testcaseIOs/{testcaseIOsFileId}",
                        problem.getId(), problem.getTestcaseIOsFileId())))
                .andExpect(status().isOk())
                .andExpect(header().longValue("Content-Length", testcaseIOsZip.length))
                .andExpect(content().contentType("application/zip"))
                .andExpect(content().bytes(testcaseIOsZip));
    }

    @Test
    void GivenTagsSaved_WhenGetAllTags_ShouldRespondAllTags() throws Exception {
        List<String> tags = givenTagsSaved("tag1", "tag2", "tag3");

        mockMvc.perform(get(API_PREFIX + "/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(toJson(tags)));
    }

    private List<String> givenTagsSaved(String... tags) {
        List<String> tagList = asList(tags);
        problemRepository.saveTags(tagList);
        return tagList;
    }

    @Test
    void GivenTaggedProblemsSaved_WhenGetProblemsThatMatchToTheTags_ShouldRespondThoseProblemItems() throws Exception {
        ProblemItem targetProblem1 = toProblemItem(
                givenProblemWithTags(1, "tag1", "tag2"));
        ProblemItem targetProblem2 = toProblemItem(
                givenProblemWithTags(2, "tag1", "tag2"));

        filterProblemsWithTagsShouldContain(adminToken, asList("tag1", "tag2"), asList(targetProblem1, targetProblem2));
        filterProblemsWithTagsShouldContain(adminToken, singletonList("tag1"), asList(targetProblem1, targetProblem2));
        filterProblemsWithTagsShouldContain(adminToken, singletonList("tag2"), asList(targetProblem1, targetProblem2));
    }

    @Test
    void GivenTaggedProblemsSaved_WhenGetProblemsThatDontMatchToTags_ShouldRespondEmptyArray() throws Exception {
        toProblemItem(givenProblemWithTags(1, "tag1", "tag2"));
        toProblemItem(givenProblemWithTags(2, "tag1", "tag2"));

        filterProblemsWithTagsShouldContain(adminToken, asList("tag1", "tag2", "tag3"), emptyList());
        filterProblemsWithTagsShouldContain(adminToken, singletonList("Non-existent-tag"), emptyList());
    }


    @Test
    void Given10ProblemsSaved_WhenGetAllProblems_ShouldRespondAll10Problems() throws Exception {
        List<Problem> problems = givenProblemsSaved(10);

        assertEquals(mapToList(problems, ProblemItem::toProblemItem),
                getProblemItems(withToken(adminToken)));
    }

    @Test
    void GivenOneProblemSaved_WhenGetProblemsWithoutPageSpecified_ShouldRespondOnlyThatProblem() throws Exception {
        Problem expectedProblem = givenOneProblemSaved();

        assertEquals(toProblemItem(expectedProblem), getProblemItems(withToken(adminToken)).get(0));
    }

    @Test
    void testProblemsPagination() throws Exception {
        List<Problem> expectedProblems = givenProblemsSaved(200);

        // Strict pagination testing
        int page = 0;
        List<ProblemItem> actualAllProblemItems = new ArrayList<>();
        Set<ProblemItem> actualProblemItemsInPreviousPage = new HashSet<>();
        List<ProblemItem> actualProblemItems;

        do {
            actualProblemItems = getProblemItemsInPage(adminToken, page);
            actualAllProblemItems.addAll(actualProblemItems);

            assertTrue(actualProblemItems.stream().noneMatch(actualProblemItemsInPreviousPage::contains),
                    "Problem duplicated in different pages.");
            actualProblemItemsInPreviousPage = new HashSet<>(actualProblemItems);
            page++;
        } while (!actualProblemItems.isEmpty());

        for (int i = 0; i < expectedProblems.size(); i++) {
            assertEquals(expectedProblems.get(i).getId(), actualAllProblemItems.get(i).id);
            assertEquals(expectedProblems.get(i).getTitle(), actualAllProblemItems.get(i).title);
        }
    }

    @Test
    void WhenSaveProblemWithTitle_ProblemShouldBeSavedAndItsIdShouldBeResponded() throws Exception {
        String randomTitle = randomUUID().toString();
        int id = saveProblemWithTitle(randomTitle);

        assertEquals(randomTitle, getProblem(withToken(adminToken), id).getTitle());
    }

    @Test
    void GivenOneProblemSaved_WhenPatchProblemWithNewTitle_TheProblemShouldHaveNewTitle() throws Exception {
        Problem expectedProblem = givenOneProblemSaved();
        String newTitle = randomUUID().toString();

        expectedProblem.setTitle(newTitle);
        patchProblem(patch -> patch.title(newTitle));

        var actualProblem = getProblem(withToken(adminToken), expectedProblem.getId());
        assertProblemEquals(toViewModel(expectedProblem), actualProblem);
    }

    @Test
    void GivenOneProblemSaved_WhenPatchProblemWithDescription_TheProblemShouldHaveNewDescription() throws Exception {
        Problem expectedProblem = givenOneProblemSaved();
        String newDescription = randomUUID().toString();

        expectedProblem.setDescription(newDescription);
        patchProblem(patch -> patch.description(newDescription));

        var actualProblem = getProblem(withToken(adminToken), expectedProblem.getId());
        assertProblemEquals(toViewModel(expectedProblem), actualProblem);
    }

    @Test
    void GivenOneProblemSaved_WhenPatchProblemWithMatchPluginTag_TheProblemShouldHaveNewMatchPluginTag() throws Exception {
        Problem expectedProblem = givenOneProblemSaved();
        JudgePluginTag pluginMatchTag = new JudgePluginTag(OUTPUT_MATCH_POLICY, "Judge Girl", "Test", "1.0");

        expectedProblem.setOutputMatchPolicyPluginTag(pluginMatchTag);
        patchProblem(patch -> patch.matchPolicyPluginTag(new PatchProblemUseCase.JudgePluginTagItem(pluginMatchTag)));

        var actualProblem = getProblem(withToken(adminToken), expectedProblem.getId());
        assertProblemEquals(toViewModel(expectedProblem), actualProblem);
    }

    @Test
    void GivenOneProblemSaved_WhenPatchProblemWithFilterPluginTags_TheProblemShouldHaveNewFilterPluginTags() throws Exception {
        Problem expectedProblem = givenOneProblemSaved();
        var filterPluginTags = new HashSet<>(
                generate(10, i -> new JudgePluginTag(
                        JudgePluginTag.Type.FILTER, "Judge Girl", format("Test %d", i), format("%d.0", i)))
        );

        expectedProblem.setFilterPluginTags(filterPluginTags);
        patchProblem(patch -> patch.filterPluginTags(mapToList(filterPluginTags, PatchProblemUseCase.JudgePluginTagItem::new)));

        var actualProblem = getProblem(withToken(adminToken), expectedProblem.getId());
        assertProblemEquals(toViewModel(expectedProblem), actualProblem);
    }

    @Test
    void GivenOneProblemSaved_WhenPatchTheProblemWithNewTags_ShouldHaveUpdatedNewTags() throws Exception {
        Problem expectedProblem = givenOneProblemSaved();

        List<String> newTags = asList("newTagA", "newTagB");
        expectedProblem.setTags(newTags);
        patchProblem(patch -> patch.tags(newTags));

        var actualProblem = getProblem(withToken(adminToken), expectedProblem.getId());
        assertProblemEquals(toViewModel(expectedProblem), actualProblem);
    }

    @Test
    void GivenOneProblemSaved_WhenPatchTheProblemToBeVisible_ThenProblemShouldBeUpdatedVisible() throws Exception {
        Problem expectedProblem = givenOneProblemSaved();

        expectedProblem.setVisible(true);
        patchProblem(patch -> patch.visible(true));

        var actualProblem = getProblem(withToken(adminToken), expectedProblem.getId());
        assertProblemEquals(toViewModel(expectedProblem), actualProblem);
    }

    @Test
    void GivenProblemSaved_WhenUpdateTestcase_ThenShouldUpdateSuccessfully() throws Exception {
        int problemId = 1;
        Problem expectedProblem = saveProblem(problemId);

        var expectedTestcaseUpdate =
                upsert(expectedProblem.getTestcases().get(0), tc -> tc.setGrade(100));
        expectedProblem.upsertTestcase(expectedTestcaseUpdate.toValue());
        upsertTestCase(problemId, expectedTestcaseUpdate);

        var actualProblem = getProblem(withToken(adminToken), problemId);
        assertProblemEquals(toViewModel(expectedProblem), actualProblem);
    }

    @Test
    void GivenProblemSaved_WhenAddNewTestcase_ThenShouldAddSuccessfully() throws Exception {
        int problemId = 1;
        Problem expectedProblem = saveProblem(problemId);

        var expectedTestcaseName = "123456";
        TestcaseUpsert testcaseUpsert = new TestcaseUpsert(expectedTestcaseName, problemId, 100, 300, 300, -1, 500);
        expectedProblem.upsertTestcase(testcaseUpsert.toValue());
        upsertTestCase(problemId, testcaseUpsert);

        var actualProblem = getProblem(withToken(adminToken), problemId);
        assertProblemEquals(toViewModel(expectedProblem), actualProblem);
    }

    @Test
    void GivenProblems_1_2_3_Saved_WhenGetProblemsByIds_1_2_3_ShouldRespondProblems1_2_3() throws Exception {
        saveProblems(1, 2, 3);

        var actualProblems = getProblems(withToken(adminToken), 1, 2, 3);

        problemsShouldHaveIds(actualProblems, 1, 2, 3);
    }

    @Test
    void Given_1_ProbplemSaved_WhenGetProblemsByIds_1_2_ShouldOnlyRespondTheProblem_1() throws Exception {
        saveProblems(1);

        var actualProblems = getProblems(withToken(adminToken), 1, 2);

        problemsShouldHaveIds(actualProblems, 1);
    }

    @Test
    void GivenOneProblemSaved_WhenArchiveIt_ShouldSucceed_AndThenDeleteIt_ThenItShouldBeDeletedAndCantBeFound() throws Exception {
        int problemId = 1;
        saveProblems(problemId);

        archiveOrDeleteProblem(problemId);

        assertTrue(problemRepository.findProblemById(problemId).orElseThrow().isArchived());

        archiveOrDeleteProblem(problemId);

        assertTrue(problemRepository.findProblemById(problemId).isEmpty());
    }

    @Test
    void GivenProblemsSaved_WhenArchiveProblemById_1_AndThenGetAllProblems_ShouldNotRespondProblem_1() throws Exception {
        givenProblemsSaved(10);

        archiveOrDeleteProblem(1);
        var problems = getProblemItems(withToken(adminToken));

        assertTrue(problems.stream().allMatch(problem -> problem.id != 1));
    }

    @Test
    void GivenOneProblemWithLangEnvC_WhenUpdateTheLangEnvC_ThenCShouldBeUpdated() throws Exception {
        int problemId = 1;
        Problem problem = saveProblem(problemId);
        LanguageEnv languageEnv = problem.getLanguageEnv(Language.C);

        var langEnvUpdate = upsertLanguageEnv(languageEnv, update -> update.setResourceSpecCpu(8));
        upsertLanguageEnv(problemId, langEnvUpdate);

        var actualProblem = getProblem(withToken(adminToken), problemId);
        var actualLangEnv = actualProblem.getLanguageEnvs().get(0);
        var expectedLangEnv = langEnvUpdate.toValue();
        expectedLangEnv.setProvidedCodesFileId(languageEnv.getProvidedCodesFileId());
        assertEquals(toViewModel(expectedLangEnv), actualLangEnv);
    }

    @Test
    void GivenOneProblemWithLangEnvC_WhenUploadProblemWithNewJavaEnv_ThenProblemShouldHaveJavaEnv() throws Exception {
        int problemId = 1;
        saveProblems(problemId);

        var newJavaEnvUpdate = upsertLanguageEnv(Language.JAVA);
        upsertLanguageEnv(problemId, newJavaEnvUpdate);

        var problem = getProblem(withToken(adminToken), problemId);
        var languageEnvs = problem.getLanguageEnvs();
        assertEquals(2, languageEnvs.size());
        var actualJavaEnv = problem.getLanguageEnv(Language.JAVA).orElseThrow();
        assertEquals(toViewModel(newJavaEnvUpdate.toValue()), actualJavaEnv);
    }

    @Test
    void GivenOneProblemSaved_WhenUploadTwoProvidedCodes_ShouldRespondProvidedCodesFileId() throws Exception {
        Language language = Language.C;
        int problemId = 1;
        saveProblems(problemId);

        String fileId = uploadProvidedCodesAndGetFileId(problemId, language, getTwoProvidedCodes());

        var problem = getProblem(withToken(adminToken), problemId);
        problemShouldHaveProvidedCodesId(problem, fileId, language);
    }

    @Test
    void WhenUploadTwoProvidedCodesWithNonExistingProblemId_ShouldRespondNotFound() throws Exception {
        Language language = Language.C;
        int nonExistingProblemId = 123;
        uploadProvidedCodes(nonExistingProblemId, language, getTwoProvidedCodes()).andExpect(status().isNotFound());
    }

    @Test
    void GivenOneProblemSavedWithoutLanguageEnv_WhenUploadProvidedCodes_ShouldRespondBadRequest() throws Exception {
        Language language = Language.C;
        int problemId = saveProblemWithTitle("problemTitle");

        uploadProvidedCodes(problemId, language, getTwoProvidedCodes())
                .andExpect(status().isBadRequest());
    }

    @Test
    void GivenOneProblemSaved_WhenArchiveIt_AndThenDeleteIt_ThenProblemProvidedCodesAndTestcaseIOsShouldBeDeleted() throws Exception {
        int problemId = 1;
        Problem problem = saveProblem(problemId);

        archiveOrDeleteProblem(problemId);

        archiveOrDeleteProblem(problemId);

        problemProvidedCodesAndTestcaseIOsShouldBeDeleted(problem);
    }

    @Test
    void GivenThreeInvisibleProblemsSaved_WhenStudentGetProblemsByIds_ThenShouldRespondEmptyProblems() throws Exception {
        Integer[] problemIds = {1, 2, 3};
        saveProblems(problemIds);

        var problems = getProblems(withToken(student1Token), problemIds);

        assertTrue(problems.isEmpty());
    }

    @Test
    void GivenThreeInvisibleProblemsSaved_WhenStudentGetProblemsByTagsAndPage_ThenShouldRespondEmptyProblems() throws Exception {
        String[] tags = {"tag1", "tag2"};
        Integer[] problemIds = {1, 2, 3};
        saveProblems(problemIds);

        var problems = getProblemsByTagsAndPage(student1Token, 0, tags);

        assertTrue(problems.isEmpty());
    }

    @Test
    void GivenThreeInvisibleProblemsSaved_WhenGuestGetProblemsByIds_ThenShouldRespondEmptyProblems() throws Exception {
        Integer[] problemIds = {1, 2, 3};
        saveProblems(problemIds);

        var problems = getProblems(problemIds);

        assertTrue(problems.isEmpty());
    }

    @Test
    void GivenOneInvisibleProblemSaved_WhenGuestGetProblem_ThenShouldRespondNotFound() throws Exception {
        int problemId = 1;
        saveProblems(problemId);

        mockMvc.perform(get(API_PREFIX + "/{problemId}", problemId))
                .andExpect(status().isNotFound());
    }

    private void archiveOrDeleteProblem(int problemId) throws Exception {
        mockMvc.perform(withToken(adminToken,
                delete(API_PREFIX + "/{problemId}", problemId)))
                .andExpect(status().isOk());
    }

    private void patchProblem(Consumer<PatchProblemUseCase.Request.RequestBuilder> patching) throws Exception {
        var requestBuilder = PatchProblemUseCase.Request.builder();
        patching.accept(requestBuilder);
        patchProblem(requestBuilder.build());
    }

    private void patchProblem(PatchProblemUseCase.Request request) throws Exception {
        mockMvc.perform(withToken(adminToken,
                patch(API_PREFIX + "/{problemId}", request.problemId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk());
    }

    private Problem givenOneProblemSaved() {
        return givenProblemsSaved(1).get(0);
    }


    private void upsertTestCase(int problemId, TestcaseUpsert testcaseUpsert) throws Exception {
        mockMvc.perform(withToken(adminToken,
                put(API_PREFIX + "/{problemId}/testcases/{testcaseId}",
                        problemId, testcaseUpsert.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(testcaseUpsert))))
                .andExpect(status().isOk());
    }

    private void problemsShouldHaveIds(List<ProblemView> actualProblems, Integer... problemIds) {
        Set<Integer> idsSet = Set.of(problemIds);
        actualProblems.forEach(problem -> assertTrue(idsSet.contains(problem.getId())));
    }

    private ProblemView getProblem(WithHeader withHeader, int problemId) throws Exception {
        var request = get(API_PREFIX + "/{problemId}", problemId);
        withHeader.decorate(request);
        return getBody(mockMvc.perform(request).andExpect(status().isOk()), ProblemView.class);
    }

    private List<ProblemView> getProblems(WithHeader withHeader, Integer... problemIds) throws Exception {
        String ids = String.join(", ", mapToList(problemIds, String::valueOf));
        var request = get(API_PREFIX).queryParam("ids", ids);
        withHeader.decorate(request);
        return getBody(mockMvc.perform(request).andExpect(status().isOk()), new TypeReference<>() {
        });
    }

    private List<ProblemView> getProblems(Integer... problemIds) throws Exception {
        return getProblems(WithHeader.empty(), problemIds);
    }

    private void saveProblems(Integer... problemIds) {
        stream(problemIds).forEach(problemId -> {
            Problem problem = problemTemplate().id(problemId).build();
            byte[] providedCodesZip = zipFilesFromResources("/stubs/file1.c", "/stubs/file2.c");
            byte[] testcaseIOsZip = zipFilesFromResources("/stubs/in/", "/stubs/out/");
            problemRepository.save(problem, singletonMap(problem.getLanguageEnv(Language.C),
                    new ByteArrayInputStream(providedCodesZip)), new ByteArrayInputStream(testcaseIOsZip));
        });
    }

    private void assertProblemEquals(ProblemView expect, ProblemView actual) {
        expect.judgeFilterPluginTags = sortToList(expect.judgeFilterPluginTags, comparing(JudgePluginTagView::toString));
        actual.judgeFilterPluginTags = sortToList(actual.judgeFilterPluginTags, comparing(JudgePluginTagView::toString));
        expect.testcases = sortToList(expect.testcases, comparing(TestcaseView::getId));
        actual.testcases = sortToList(actual.testcases, comparing(TestcaseView::getId));
        expect.tags = sortToList(expect.tags);
        actual.tags = sortToList(actual.tags);
        expect.languageEnvs = sortToList(expect.languageEnvs, comparing(LanguageEnvView::getLanguage));
        actual.languageEnvs = sortToList(actual.languageEnvs, comparing(LanguageEnvView::getLanguage));
        assertEquals(expect, actual);
    }

    private LanguageEnvUpsert upsertLanguageEnv(Language language) {
        return LanguageEnvUpsert.fromLangEnv(languageEnvTemplate(language).build());
    }

    private LanguageEnvUpsert upsertLanguageEnv(LanguageEnv languageEnv, Consumer<LanguageEnvUpsert> update) {
        return LanguageEnvUpsert.upsert(languageEnv, update);
    }

    private LanguageEnvUpsert upsertLanguageEnv(Language language, Consumer<LanguageEnvUpsert> update) {
        return LanguageEnvUpsert.upsert(languageEnvTemplate(language).build(), update);
    }

    private void upsertLanguageEnv(Integer problemId, LanguageEnvUpsert languageEnv) throws Exception {
        mockMvc.perform(withToken(adminToken,
                put(API_PREFIX + "/{problemId}/langEnv/{langEnv}",
                        problemId, languageEnv.getLanguage())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(toJson(languageEnv))))
                .andExpect(status().isOk());
    }

    private Problem saveProblem(int problemId) {
        saveProblems(problemId);
        return problemRepository.findProblemById(problemId).orElseThrow();
    }

    private int saveProblemWithTitle(String title) throws Exception {
        return parseInt(getContentAsString(
                mockMvc.perform(withToken(adminToken,
                        post(API_PREFIX)
                                .contentType(MediaType.TEXT_PLAIN_VALUE).content(title)))
                        .andExpect(status().isOk())));
    }

    private Problem givenProblemSavedWithProvidedCodesAndTestcaseIOs() {
        providedCodesZip = zipFilesFromResources("/stubs/file1.c", "/stubs/file2.c");
        testcaseIOsZip = zipFilesFromResources("/stubs/in/", "/stubs/out/");

        Problem problem = problemTemplate().build();
        return problemRepository.save(problem,
                singletonMap(problem.getLanguageEnv(Language.C), new ByteArrayInputStream(providedCodesZip)),
                new ByteArrayInputStream(testcaseIOsZip));
    }

    private List<ProblemItem> getProblemItems(WithHeader withHeader) throws Exception {
        var request = get(API_PREFIX);
        withHeader.decorate(request);
        return getBody(mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)), new TypeReference<>() {
        });
    }

    private List<ProblemItem> getProblemItemsInPage(Token token, int page) throws Exception {
        return getBody(mockMvc.perform(withToken(token, get(API_PREFIX)
                .queryParam("page", String.valueOf(page))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)), new TypeReference<>() {
        });
    }

    private List<Problem> givenProblemsSaved(int count) {
        Random random = new Random();
        var problems = range(0, count)
                .mapToObj((id) ->
                        problemTemplate().id(id)
                                .title(String.valueOf(random.nextInt())).build())
                .collect(toList());
        problems.forEach(problemRepository::save);
        return problems;
    }

    private Problem givenProblemWithTags(int problemId, String... tags) {
        Problem targetProblem = problemTemplate().id(problemId).tags(asList(tags)).build();
        return problemRepository.save(targetProblem);
    }

    private void filterProblemsWithTagsShouldContain(Token token, List<String> tags, List<ProblemItem> problemItems) throws Exception {
        String tagsSplitByCommas = String.join(", ", tags);
        mockMvc.perform(get(API_PREFIX)
                .header("Authorization", bearerWithToken(token.getToken()))
                .queryParam("tags", tagsSplitByCommas))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(problemItems)));
    }


    private List<ProblemView> getProblemsByTagsAndPage(Token token, int page, String... tags) throws Exception {
        String tagsSplitByCommas = String.join(",", tags);
        return getBody(mockMvc.perform(get(API_PREFIX)
                .header("Authorization", bearerWithToken(token.getToken()))
                .queryParam("tags", tagsSplitByCommas)
                .queryParam("page", String.valueOf(page)))
                .andExpect(status().isOk()), new TypeReference<>() {
        });
    }

    private MockMultipartFile[] getTwoProvidedCodes() throws IOException {
        return new MockMultipartFile[]{
                new MockMultipartFile(PROVIDED_CODE_MULTIPART_KEY_NAME, "file1.c", "text/plain",
                        ResourceUtils.getResourceAsStream("/stubs/file1.c")),
                new MockMultipartFile(PROVIDED_CODE_MULTIPART_KEY_NAME, "file2.c", "text/plain",
                        ResourceUtils.getResourceAsStream("/stubs/file2.c"))
        };
    }

    private ResultActions uploadProvidedCodes(int problemId, Language language, MockMultipartFile[] files) throws Exception {
        return mockMvc.perform(multipartRequestWithProvidedCodes(problemId, language, files));
    }

    private String uploadProvidedCodesAndGetFileId(int problemId, Language language, MockMultipartFile... files) throws Exception {
        return getContentAsString(uploadProvidedCodes(problemId, language, files)
                .andExpect(status().isOk()));
    }

    private MockHttpServletRequestBuilder multipartRequestWithProvidedCodes(int problemId, Language language, MockMultipartFile... files) {
        var call = multipart(API_PREFIX + "/{problemId}/{langEnvName}/providedCodes", problemId, language.toString());

        call.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        for (MockMultipartFile file : files) {
            call = call.file(file);
        }
        return withToken(adminToken, call);
    }

    private void problemShouldHaveProvidedCodesId(ProblemView problem, String fileId, Language language) {
        findFirst(problem.languageEnvs, langEnv -> langEnv.getLanguage().equals(language))
                .ifPresent(langEnv -> assertEquals(fileId, langEnv.getProvidedCodesFileId()));
    }

    private void problemProvidedCodesAndTestcaseIOsShouldBeDeleted(Problem problem) {
        List<String> providedCodes = mapToList(problem.getLanguageEnvs().values(), LanguageEnv::getProvidedCodesFileId);
        List<String> fileIds = new LinkedList<>(providedCodes);
        fileIds.add(problem.getTestcaseIOsFileId());
        fileIds.forEach(fileId -> assertFalse(existsFile(fileId)));
    }

    private boolean existsFile(String fileId) {
        return ofNullable(gridFsTemplate.findOne(new Query(where("_id").is(fileId))))
                .map(gridFsTemplate::getResource)
                .map(GridFsResource::exists)
                .orElse(false);
    }

    private ProblemView toViewModel(Problem problem) {
        return ProblemView.toViewModel(problem);
    }

    private LanguageEnvView toViewModel(LanguageEnv languageEnv) {
        return LanguageEnvView.toViewModel(languageEnv);
    }

}

