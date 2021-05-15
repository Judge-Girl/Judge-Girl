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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tw.waterball.judgegirl.commons.utils.ResourceUtils;
import tw.waterball.judgegirl.commons.utils.ZipUtils;
import tw.waterball.judgegirl.primitives.problem.*;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;
import tw.waterball.judgegirl.problem.domain.usecases.PatchProblemUseCase;
import tw.waterball.judgegirl.problemapi.views.ProblemItem;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.springboot.problem.SpringBootProblemApplication;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.findFirst;
import static tw.waterball.judgegirl.primitives.stubs.ProblemStubs.problemTemplate;
import static tw.waterball.judgegirl.problem.domain.usecases.UploadProvidedCodeUseCase.PROVIDED_CODE_MULTIPART_KEY_NAME;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@ActiveProfiles({Profiles.JWT, Profiles.EMBEDDED_MONGO})
@AutoConfigureDataMongo
@ContextConfiguration(classes = SpringBootProblemApplication.class)
public class ProblemControllerTest extends AbstractSpringBootTest {

    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    ProblemRepository problemRepository;

    private Problem problem;
    private byte[] providedCodesZip;
    private byte[] testcaseIOsZip;

    @BeforeEach
    void setup() {
        problem = problemTemplate()
                .build();
    }

    @AfterEach
    void clean() {
        problemRepository.deleteAll();
    }

    private void givenProblemSavedWithProvidedCodesAndTestcaseIOs() {
        providedCodesZip = ZipUtils.zipFilesFromResources("/stubs/file1.c", "/stubs/file2.c");
        testcaseIOsZip = ZipUtils.zipFilesFromResources("/stubs/in/", "/stubs/out/");

        this.problem = problemRepository.save(problem,
                singletonMap(problem.getLanguageEnv(Language.C), new ByteArrayInputStream(providedCodesZip))
                , new ByteArrayInputStream(testcaseIOsZip));
    }

    @Test
    void GivenProblemSaved_DownloadZippedProvidedCodesShouldSucceed() throws Exception {
        givenProblemSavedWithProvidedCodesAndTestcaseIOs();

        LanguageEnv languageEnv = problem.getLanguageEnv(Language.C);
        mockMvc.perform(get("/api/problems/{problemId}/{languageEnv}/providedCodes/{providedCodesFileId}",
                problem.getId(), languageEnv.getName(), languageEnv.getProvidedCodesFileId()))
                .andExpect(status().isOk())
                .andExpect(header().longValue("Content-Length", providedCodesZip.length))
                .andExpect(content().contentType("application/zip"))
                .andExpect(content().bytes(providedCodesZip));
    }

    @Test
    void GivenProblemSaved_WhenGetProblemById_ShouldRespondThatProblem() throws Exception {
        givenProblemSavedWithProvidedCodesAndTestcaseIOs();

        mockMvc.perform(get("/api/problems/{problemId}", problem.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(toJson(ProblemView.toViewModel(problem))));
    }

    @Test
    void GivenProblemSaved_DownloadZippedTestCaseIOsShouldSucceed() throws Exception {
        givenProblemSavedWithProvidedCodesAndTestcaseIOs();

        mockMvc.perform(get("/api/problems/{problemId}/testcaseIOs/{testcaseIOsFileId}",
                problem.getId(), problem.getTestcaseIOsFileId()))
                .andExpect(status().isOk())
                .andExpect(header().longValue("Content-Length", testcaseIOsZip.length))
                .andExpect(content().contentType("application/zip"))
                .andExpect(content().bytes(testcaseIOsZip));
    }

    @Test
    void GivenTagsSaved_WhenGetAllTags_ShouldRespondAllTags() throws Exception {
        final List<String> tags = givenTagsSaved("tag1", "tag2", "tag3");

        mockMvc.perform(get("/api/problems/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(toJson(tags)));
    }

    @NotNull
    private List<String> givenTagsSaved(String... tags) {
        final List<String> tagList = asList(tags);
        problemRepository.saveTags(tagList);
        return tagList;
    }

    @Test
    void GivenTaggedProblemsSaved_WhenGetProblemsThatMatchToTags_ShouldRespondThoseProblemItems() throws Exception {
        final ProblemItem targetProblem1 = ProblemItem.fromEntity(
                givenProblemWithTags(1, "tag1", "tag2"));
        final ProblemItem targetProblem2 = ProblemItem.fromEntity(
                givenProblemWithTags(2, "tag1", "tag2"));

        verifyFindProblemsByTagsWithExpectedList(asList("tag1", "tag2"), asList(targetProblem1, targetProblem2));
        verifyFindProblemsByTagsWithExpectedList(singletonList("tag1"), asList(targetProblem1, targetProblem2));
        verifyFindProblemsByTagsWithExpectedList(singletonList("tag2"), asList(targetProblem1, targetProblem2));
    }

    @Test
    void GivenTaggedProblemsSaved_WhenGetProblemsToDontMatchToTags_ShouldRespondEmptyArray() throws Exception {
        ProblemItem.fromEntity(givenProblemWithTags(1, "tag1", "tag2"));
        ProblemItem.fromEntity(givenProblemWithTags(2, "tag1", "tag2"));

        verifyFindProblemsByTagsWithExpectedList(asList("tag1", "tag2", "tag3"), emptyList());
        verifyFindProblemsByTagsWithExpectedList(singletonList("Non-existent-tag"), emptyList());
    }

    private Problem givenProblemWithTags(int id, String... tags) {
        final Problem targetProblem = problemTemplate().id(id)
                .tags(asList(tags)).build();
        problemRepository.save(targetProblem);
        return targetProblem;
    }

    private void verifyFindProblemsByTagsWithExpectedList(List<String> tags, List<ProblemItem> problemItems) throws Exception {
        String tagsSplitByCommas = String.join(", ", tags);

        mockMvc.perform(get("/api/problems")
                .queryParam("tags", tagsSplitByCommas))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(problemItems)));
    }

    @Test
    void GivenProblemsSaved_WhenGetAllProblems_ShouldRespondAll() throws Exception {
        List<Problem> problems = givenProblemsSaved(10);

        // verify all problems will be found and projected into problem-items
        assertEquals(problems.stream()
                .map(ProblemItem::fromEntity)
                .collect(toList()), requestGetProblems());
    }

    @Test
    void GivenOneProblemSaved_WhenGetProblemsWithoutPageSpecified_ShouldRespondOnlyThatProblem() throws Exception {
        Problem expectedProblem = givenProblemsSaved(1).get(0);

        assertEquals(ProblemItem.fromEntity(expectedProblem), requestGetProblems().get(0));
    }

    @Test
    void GivenManyProblemsSaved_WhenGetProblemsInPage_ShouldRespondOnlyThoseProblemsInThatPage() throws Exception {
        List<Problem> expectedProblems = givenProblemsSaved(200);

        // Strict pagination testing
        int page = 0;
        List<ProblemItem> actualAllProblemItems = new ArrayList<>();
        Set<ProblemItem> actualProblemItemsInPreviousPage = new HashSet<>();
        List<ProblemItem> actualProblemItems;

        do {
            actualProblemItems = requestGetProblemsInPage(page);
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

    private List<ProblemItem> requestGetProblems() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/problems"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
    }

    private List<ProblemItem> requestGetProblemsInPage(int page) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/problems?page={page}", page))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
    }

    private List<Problem> givenProblemsSaved(int count) {
        Random random = new Random();
        var problems = IntStream.range(0, count).mapToObj((id) ->
                problemTemplate().id(id)
                        .title(String.valueOf(random.nextInt())).build())
                .collect(toList());
        problems.forEach(problemRepository::save);
        return problems;
    }

    @Test
    void WhenSaveProblemWithTitle_ProblemShouldBeSavedAndRespondItsId() throws Exception {
        String randomTitle = UUID.randomUUID().toString();
        int id = saveProblemWithTitle(randomTitle);

        assertEquals(randomTitle, problemRepository.findProblemById(id).orElseThrow().getTitle());
    }

    private int saveProblemWithTitle(String title) throws Exception {
        return parseInt(getContentAsString(
                mockMvc.perform(post("/api/problems")
                        .contentType(MediaType.TEXT_PLAIN_VALUE).content(title))
                        .andExpect(status().isOk())));
    }

    @Test
    void GivenOneProblemSavedAndPatchProblemWithNewTitle_WhenQueryTheSameProblem_ShouldHaveNewTitle() throws Exception {
        Problem savedProblem = givenOneProblemSaved();
        String newTitle = UUID.randomUUID().toString();

        savedProblem.setTitle(newTitle);
        PatchProblemUseCase.Request request = PatchProblemUseCase.Request.builder().title(newTitle).build();
        patchProblem(request);

        Problem actualProblem = problemRepository.findProblemById(savedProblem.getId())
                .orElseThrow();
        assertNotNull(actualProblem);
        assertEquals(newTitle, actualProblem.getTitle());
    }

    @Test
    void GivenOneProblemSavedAndPatchProblemWithDescription_WhenQueryById_ShouldHaveNewDescription() throws Exception {
        Problem savedProblem = givenOneProblemSaved();
        String newDescription = UUID.randomUUID().toString();

        savedProblem.setDescription(newDescription);
        PatchProblemUseCase.Request request = PatchProblemUseCase.Request.builder().description(newDescription).build();
        patchProblem(request);

        Problem actualProblem = problemRepository.findProblemById(savedProblem.getId()).orElseThrow();
        assertNotNull(actualProblem);
        assertEquals(newDescription, actualProblem.getDescription());
    }

    @Test
    void GivenOneProblemSavedAndPatchProblemWithPluginMatchTags_WhenQueryById_ShouldHaveNewTags() throws Exception {
        Problem savedProblem = givenOneProblemSaved();
        JudgePluginTag pluginMatchTag = new JudgePluginTag();
        pluginMatchTag.setGroup("Judge Girl");
        pluginMatchTag.setName("Test");
        pluginMatchTag.setVersion("1.0");
        pluginMatchTag.setType(JudgePluginTag.Type.OUTPUT_MATCH_POLICY);

        savedProblem.setOutputMatchPolicyPluginTag(pluginMatchTag);
        PatchProblemUseCase.Request request = PatchProblemUseCase.Request.builder().matchPolicyPluginTag(pluginMatchTag).build();
        patchProblem(request);

        Problem actualProblem = problemRepository.findProblemById(savedProblem.getId()).orElseThrow();
        assertNotNull(actualProblem);
        JudgePluginTag actualPluginMatchTag = actualProblem.getOutputMatchPolicyPluginTag();
        assertEquals(pluginMatchTag, actualPluginMatchTag);
    }

    @Test
    void GivenOneProblemSavedAndPatchProblemWithPluginFilterTags_WhenQueryById_ShouldHaveNewTags() throws Exception {
        Problem savedProblem = givenOneProblemSaved();
        Set<JudgePluginTag> filterPluginTags = new HashSet<>();
        final int COUNT_FILTER = 10;
        for (int i = 1; i <= COUNT_FILTER; ++i) {
            JudgePluginTag filterPluginTag = new JudgePluginTag();
            filterPluginTag.setGroup("Judge Girl");
            filterPluginTag.setName(String.format("Test %d", i));
            filterPluginTag.setVersion(String.format("%d.0", i));
            filterPluginTag.setType(JudgePluginTag.Type.FILTER);
            filterPluginTags.add(filterPluginTag);
        }

        savedProblem.setFilterPluginTags(filterPluginTags);
        PatchProblemUseCase.Request request = PatchProblemUseCase.Request.builder().filterPluginTags(filterPluginTags).build();
        patchProblem(request);

        Problem actualProblem = problemRepository.findProblemById(savedProblem.getId()).orElseThrow();
        assertNotNull(actualProblem);
        Set<JudgePluginTag> queryPluginFilterTags = new HashSet<>(actualProblem.getFilterPluginTags());
        assertEquals(queryPluginFilterTags, filterPluginTags);
    }


    private void patchProblem(PatchProblemUseCase.Request request) throws Exception {
        mockMvc.perform(patch("/api/problems/{problemId}", problem.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(
                        request)))
                .andExpect(status().isOk());
    }

    private Problem givenOneProblemSaved() {
        return givenProblemsSaved(1).get(0);
    }

    @Test
    void GivenProblems_1_2_3_Saved_WhenGetProblemsByIds_1_2_3_ShouldRespondThat1_2_3() throws Exception {
        saveProblems(1, 2, 3);

        List<Problem> actualProblems = getProblems(1, 2, 3);

        problemsShouldHaveIds(actualProblems, 1, 2, 3);
    }

    @Test
    void Given_1_ProblemSaved_WhenGetProblemsByIds_1_2_ShouldRespondThat_1() throws Exception {
        saveProblems(1);

        List<Problem> actualProblems = getProblems(1, 2);

        problemsShouldHaveIds(actualProblems, 1);
    }

    @Test
    void GivenOneProblemSaved_WhenArchiveIt_ItShouldSucceed_AndThenDeleteIt_ThenItShouldBeDeletedAndCantBeFound() throws Exception {
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
        var problems = requestGetProblems();

        assertTrue(problems.stream().allMatch(problem -> problem.id != 1));
    }

    private void archiveOrDeleteProblem(int problemId) throws Exception {
        mockMvc.perform(delete("/api/problems/{problemId}", problemId))
                .andExpect(status().isOk());
    }

    @Test
    void GivenProblemSaved_WhenUpdateTestcase_ThenShouldUpdateSuccessfully() throws Exception {
        int problemId = 1;
        Problem problem = saveProblemAndGet(problemId);

        Testcase expectedTestcase = problem.getTestcases().get(0);
        String testCaseId = expectedTestcase.getId();
        int expectedTestcaseGrade = 100;
        expectedTestcase.setGrade(expectedTestcaseGrade);
        updateOrAddTestCase(problemId, testCaseId, expectedTestcase);

        var actualProblem = getProblem(problemId);
        List<Testcase> testcases = actualProblem.getTestcases();
        assertEquals(problem.getTestcases().size(), testcases.size());
        Testcase actualTestCase = findFirst(testcases, tc -> 100 == tc.getGrade()).orElseThrow();
        assertTestCaseEquals(expectedTestcase, actualTestCase);
    }

    @Test
    void GivenProblemSaved_WhenAddNewTestcase_ThenShouldAddSuccessfully() throws Exception {
        int problemId = 1;
        Problem problem = saveProblemAndGet(problemId);

        var expectedTestcaseName = "123456";
        Testcase expectedTestcase = new Testcase(expectedTestcaseName, problemId, 100, 300, 300, -100, 500);
        updateOrAddTestCase(problemId, expectedTestcaseName, expectedTestcase);

        List<Testcase> testcases = getProblem(problemId).getTestcases();
        assertEquals(problem.getTestcases().size() + 1, testcases.size());
        Testcase actualTestcase = findFirst(testcases, testcase -> expectedTestcaseName.equals(testcase.getName())).orElseThrow();
        assertNotNull(actualTestcase.getId());
        assertTestCaseEquals(expectedTestcase, actualTestcase);
    }

    private void assertTestCaseEquals(Testcase expectedTestcase, Testcase actualTestCase) {
        assertEquals(expectedTestcase.getName(), actualTestCase.getName());
        assertEquals(expectedTestcase.getProblemId(), actualTestCase.getProblemId());
        assertEquals(expectedTestcase.getTimeLimit(), actualTestCase.getTimeLimit());
        assertEquals(expectedTestcase.getMemoryLimit(), actualTestCase.getMemoryLimit());
        assertEquals(expectedTestcase.getOutputLimit(), actualTestCase.getOutputLimit());
        assertEquals(expectedTestcase.getThreadNumberLimit(), actualTestCase.getThreadNumberLimit());
        assertEquals(expectedTestcase.getGrade(), actualTestCase.getGrade());
    }

    private void updateOrAddTestCase(int problemId, String testCaseName, Testcase testcase) throws Exception {
        mockMvc.perform(put("/api/problems/{problemId}/testcases/{testcaseName}", problemId, testCaseName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(testcase)))
                .andExpect(status().isOk());
    }

    private void problemsShouldHaveIds(List<Problem> actualProblems, Integer... problemIds) {
        Set<Integer> idsSet = Set.of(problemIds);
        actualProblems.forEach(problem -> assertTrue(idsSet.contains(problem.getId())));
    }

    private ProblemView getProblem(int problemId) throws Exception {
        return getBody(mockMvc.perform(get("/api/problems/{problemId}", problemId))
                .andExpect(status().isOk()), ProblemView.class);
    }

    private List<Problem> getProblems(Integer... problemIds) throws Exception {
        return getBody(mockMvc.perform(get("/api/problems").queryParam("ids", Arrays.stream(problemIds).map(String::valueOf).collect(Collectors.joining(","))))
                .andExpect(status().isOk()), new TypeReference<>() {
        });
    }

    private void saveProblems(int... problemIds) {
        Arrays.stream(problemIds).forEach(problemId -> {
            Problem problem = problemTemplate()
                    .build();
            problem.setId(problemId);
            byte[] providedCodesZip = ZipUtils.zipFilesFromResources("/stubs/file1.c", "/stubs/file2.c");

            byte[] testcaseIOsZip = ZipUtils.zipFilesFromResources("/stubs/in/", "/stubs/out/");

            problemRepository.save(problem,
                    singletonMap(problem.getLanguageEnv(Language.C), new ByteArrayInputStream(providedCodesZip))
                    , new ByteArrayInputStream(testcaseIOsZip));
        });
    }

    @Test
    void GivenOneProblemWithLangEnvC_WhenUpdateTheLangEnvC_ThenCShouldBeUpdated() throws Exception {
        int problemId = 1;
        LanguageEnv languageEnv = saveProblemAndGet(problemId).getLanguageEnv(Language.C);
        ResourceSpec expectResourceSpec = new ResourceSpec(9999, 9999);
        languageEnv.setResourceSpec(expectResourceSpec);

        updateLanguageEnv(problemId, languageEnv);

        var problem = getProblem(problemId);
        ResourceSpec actualResourceSpec = problem.getLanguageEnvs().get(0).getResourceSpec();
        assertEquals(expectResourceSpec, actualResourceSpec);
    }

    @Test
    void GivenOneProblemWithLangEnv_C_WhenPatchProblemWithJAVA_ThenProblemShouldHaveJAVA() throws Exception {
        int problemId = 1;
        saveProblems(problemId);

        LanguageEnv languageEnv = createLanguageEnv(Language.JAVA);
        ResourceSpec expectResourceSpec = new ResourceSpec(9999, 9999);
        languageEnv.setResourceSpec(expectResourceSpec);
        updateLanguageEnv(problemId, languageEnv);

        var problem = getProblem(problemId);
        List<LanguageEnv> languageEnvs = problem.getLanguageEnvs();
        assertEquals(2, languageEnvs.size());
        LanguageEnv actualLanguageEnv = languageEnvs.get(1);
        assertEquals(expectResourceSpec, actualLanguageEnv.getResourceSpec());
    }

    private LanguageEnv createLanguageEnv(Language language) {
        return LanguageEnv.builder()
                .language(language)
                .compilation(new Compilation("script"))
                .resourceSpec(new ResourceSpec(0.5f, 0))
                .submittedCodeSpec(new SubmittedCodeSpec(language, "main." + language.getFileExtension()))
                .providedCodesFileId("providedCodesFileId")
                .build();
    }

    private void updateLanguageEnv(Integer problemId, LanguageEnv languageEnv) throws Exception {
        mockMvc.perform(put("/api/problems/{problemId}/langEnv/{langEnv}", problemId, languageEnv.getLanguage())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(toJson(languageEnv)))
                .andExpect(status().isOk());
    }

    private Problem saveProblemAndGet(int problemId) {
        saveProblems(problemId);
        return problemRepository.findProblemById(problemId).orElseThrow();
    }

    @Test
    void GiveOneProblemSaved_WhenUploadTwoProvidedCodes_ShouldRespondProvidedCodesFileId() throws Exception {
        Language language = Language.C;
        int problemId = 1;
        saveProblems(problemId);

        String fileId = uploadProvidedCodesAndGetFileId(problemId, language, getTwoProvidedCodes());

        ProblemView problem = getProblem(problemId);
        problemShouldHaveProvidedCodesId(problem, fileId, language);
    }

    @Test
    void WhenUploadTwoProvidedCodesWithNonExistingProblemId_ShouldRespondNotFound() throws Exception {
        Language language = Language.C;
        int nonExistingProblemId = 123;
        uploadProvidedCodes(nonExistingProblemId, language, getTwoProvidedCodes()).andExpect(status().isNotFound());
    }

    @Test
    void GiveOneProblemSavedWithoutLanguageEnv_WhenUploadTwoProvidedCodes_ShouldRespondProvidedCodesFileIds() throws Exception {
        Language language = Language.C;
        int problemId = saveProblemWithTitle("problemTitle");

        String fileId = uploadProvidedCodesAndGetFileId(problemId, language, getTwoProvidedCodes());

        ProblemView problem = getProblem(problemId);
        problemShouldHaveProvidedCodesId(problem, fileId, language);
    }

    private MockMultipartFile[] getTwoProvidedCodes() throws IOException {
        return new MockMultipartFile[]{
                new MockMultipartFile(PROVIDED_CODE_MULTIPART_KEY_NAME, "file1.c", "text/plain",
                        ResourceUtils.getResourceAsStream("/stubs/file1.c")),
                new MockMultipartFile(PROVIDED_CODE_MULTIPART_KEY_NAME, "file2.c", "text/plain",
                        ResourceUtils.getResourceAsStream("/stubs/file2.c"))
        };
    }

    private String uploadProvidedCodesAndGetFileId(int problemId, Language language, MockMultipartFile... files) throws Exception {
        return getContentAsString(uploadProvidedCodes(problemId, language, files)
                .andExpect(status().isOk()));
    }

    private ResultActions uploadProvidedCodes(int problemId, Language language, MockMultipartFile[] files) throws Exception {
        return mockMvc.perform(multipartRequestWithProvidedCodes(problemId, language, files));
    }

    private MockHttpServletRequestBuilder multipartRequestWithProvidedCodes(int problemId, Language language, MockMultipartFile... files) {
        var call = multipart("/api/problems/{problemId}/{langEnvName}/providedCodes", problemId, language.toString());

        call.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        for (MockMultipartFile file : files) {
            call = call.file(file);
        }
        return call;
    }

    private void problemShouldHaveProvidedCodesId(ProblemView problem, String fileId, Language language) {
        problem.languageEnvs.forEach(langEnv -> {
            if (langEnv.getLanguage().equals(language)) {
                assertEquals(fileId, langEnv.getProvidedCodesFileId());
            }
        });
    }
}

