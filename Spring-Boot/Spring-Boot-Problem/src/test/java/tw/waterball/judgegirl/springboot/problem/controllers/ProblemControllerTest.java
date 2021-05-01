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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import tw.waterball.judgegirl.commons.utils.ZipUtils;
import tw.waterball.judgegirl.primitives.problem.*;
import tw.waterball.judgegirl.primitives.stubs.ProblemStubs;
import tw.waterball.judgegirl.problemapi.views.ProblemItem;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;
import tw.waterball.judgegirl.problem.domain.usecases.PatchProblemUseCase;
import tw.waterball.judgegirl.springboot.problem.SpringBootProblemApplication;
import tw.waterball.judgegirl.springboot.problem.repositories.MongoProblemRepository;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static tw.waterball.judgegirl.primitives.problem.Language.C;
import static tw.waterball.judgegirl.primitives.problem.Language.JAVA;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@ActiveProfiles({Profiles.JWT, Profiles.EMBEDDED_MONGO})
@AutoConfigureDataMongo
@ContextConfiguration(classes = SpringBootProblemApplication.class)
public class ProblemControllerTest extends AbstractSpringBootTest {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    ProblemRepository problemRepository;

    private Problem problem;
    private byte[] providedCodesZip;
    private byte[] testcaseIOsZip;

    @BeforeEach
    void setup() {
        problem = ProblemStubs
                .problemTemplate()
                .build();
    }

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection(Problem.class);
        mongoTemplate.dropCollection(Testcase.class);
    }

    private void givenProblemSavedWithProvidedCodesAndTestcaseIOs() {
        providedCodesZip = ZipUtils.zipFilesFromResources("/stubs/file1.c", "/stubs/file2.c");
        testcaseIOsZip = ZipUtils.zipFilesFromResources("/stubs/in/", "/stubs/out/");

        this.problem = problemRepository.save(problem,
                singletonMap(problem.getLanguageEnv(C), new ByteArrayInputStream(providedCodesZip))
                , new ByteArrayInputStream(testcaseIOsZip));
    }

    @Test
    void GivenProblemSaved_DownloadZippedProvidedCodesShouldSucceed() throws Exception {
        givenProblemSavedWithProvidedCodesAndTestcaseIOs();

        LanguageEnv languageEnv = problem.getLanguageEnv(C);
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
        mongoTemplate.save(new MongoProblemRepository.AllTags(tagList));
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
        final Problem targetProblem = ProblemStubs.problemTemplate().id(id)
                .tags(asList(tags)).build();
        mongoTemplate.save(targetProblem);
        return targetProblem;
    }

    private void verifyFindProblemsByTagsWithExpectedList(List<String> tags, List<ProblemItem> problemItems) throws Exception {
        String tagsSplitByCommas = String.join(", ", tags);

        mockMvc.perform(get("/api/problems?tags={tags}", tagsSplitByCommas))
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
        List<Problem> problems = IntStream.range(0, count).mapToObj((id) ->
                ProblemStubs.problemTemplate().id(id)
                        .title(String.valueOf(random.nextInt())).build())
                .collect(toList());
        problems.forEach(mongoTemplate::save);
        return problems;
    }

    @Test
    void WhenSaveProblemWithTitle_ProblemShouldBeSavedAndRespondItsId() throws Exception {
        String randomTitle = UUID.randomUUID().toString();
        int id = parseInt(getContentAsString(
                mockMvc.perform(post("/api/problems")
                        .contentType(MediaType.TEXT_PLAIN_VALUE).content(randomTitle))
                        .andExpect(status().isOk())));

        assertEquals(randomTitle, requireNonNull(mongoTemplate.findById(id, Problem.class)).getTitle());
    }

    @Test
    void GivenOneProblemSavedAndPatchProblemWithNewTitle_WhenQueryTheSameProblem_ShouldHaveNewTitle() throws Exception {
        Problem savedProblem = givenOneProblemSaved();
        String newTitle = UUID.randomUUID().toString();

        savedProblem.setTitle(newTitle);
        patchProblem(savedProblem);

        Problem actualProblem = mongoTemplate.findById(savedProblem.getId(), Problem.class);
        assertNotNull(actualProblem);
        assertEquals(newTitle, actualProblem.getTitle());
    }

    @Test
    void GivenOneProblemSavedAndPatchProblemWithDescription_WhenQueryById_ShouldHaveNewDescription() throws Exception {
        Problem savedProblem = givenOneProblemSaved();
        String newDescription = UUID.randomUUID().toString();

        savedProblem.setDescription(newDescription);
        patchProblem(savedProblem);

        Problem actualProblem = mongoTemplate.findById(savedProblem.getId(), Problem.class);
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
        patchProblem(savedProblem);

        Problem actualProblem = mongoTemplate.findById(savedProblem.getId(), Problem.class);
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
        patchProblem(savedProblem);

        Problem actualProblem = mongoTemplate.findById(savedProblem.getId(), Problem.class);
        assertNotNull(actualProblem);
        Set<JudgePluginTag> queryPluginFilterTags = new HashSet<>(actualProblem.getFilterPluginTags());
        assertEquals(queryPluginFilterTags, filterPluginTags);
    }

    private void patchProblem(Problem problem) throws Exception {
        mockMvc.perform(patch("/api/problems/{problemId}", problem.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(
                        new PatchProblemUseCase.Request(
                                problem.getId(),
                                problem.getTitle(),
                                problem.getDescription(),
                                problem.getOutputMatchPolicyPluginTag(),
                                new HashSet<>(problem.getFilterPluginTags()),
                                problem.getLanguageEnv(C)))))
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
            Problem problem = ProblemStubs
                    .problemTemplate()
                    .build();
            problem.setId(problemId);
            byte[] providedCodesZip = ZipUtils.zipFilesFromResources("/stubs/file1.c", "/stubs/file2.c");

            byte[] testcaseIOsZip = ZipUtils.zipFilesFromResources("/stubs/in/", "/stubs/out/");

            problemRepository.save(problem,
                    singletonMap(problem.getLanguageEnv(C), new ByteArrayInputStream(providedCodesZip))
                    , new ByteArrayInputStream(testcaseIOsZip));
        });
    }

    @Test
    void GivenOneProblemWithLangEnvC_WhenUpdateTheLangEnvC_ThenCShouldBeUpdated() throws Exception {
        int problemId = 1;
        LanguageEnv languageEnv = saveProblemAndGet(problemId).getLanguageEnv(C);
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

        LanguageEnv languageEnv = createLanguageEnv(JAVA);
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
        mockMvc.perform(put("/api/problems/{problemId}/langEnv/{langEnv}", problemId, languageEnv.getLanguage().toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(toJson(languageEnv)))
                .andExpect(status().isOk());
    }
    private Problem saveProblemAndGet(int problemId) {
        saveProblems(problemId);
        return problemRepository.findProblemById(problemId).orElseThrow();
    }
}

