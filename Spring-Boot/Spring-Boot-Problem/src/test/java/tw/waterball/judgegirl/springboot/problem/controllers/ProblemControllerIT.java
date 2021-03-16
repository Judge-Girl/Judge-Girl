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
import tw.waterball.judgegirl.entities.problem.Language;
import tw.waterball.judgegirl.entities.problem.LanguageEnv;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.problem.Testcase;
import tw.waterball.judgegirl.entities.stubs.ProblemStubs;
import tw.waterball.judgegirl.problemapi.views.ProblemItem;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemRepository;
import tw.waterball.judgegirl.springboot.problem.SpringBootProblemApplication;
import tw.waterball.judgegirl.springboot.problem.repositories.MongoProblemRepository;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@ActiveProfiles({Profiles.PROD, Profiles.MONGO})
@AutoConfigureDataMongo
@ContextConfiguration(classes = SpringBootProblemApplication.class)
class ProblemControllerIT extends AbstractSpringBootTest {
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
                .template()
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
                .andExpect(content().json(toJson(ProblemView.fromEntity(problem))));
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
        final Problem targetProblem = ProblemStubs.template().id(id)
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
                .collect(Collectors.toList()), requestGetProblems());
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
                ProblemStubs.template().id(id)
                        .title(String.valueOf(random.nextInt())).build())
                .collect(Collectors.toList());
        problems.forEach(mongoTemplate::save);
        return problems;
    }

    @Test
    void GivenDoPostApiProblemsTitle_WhenReturnHTTPStatusOk_ShouldExistInDatabase() throws Exception {
        Random random = new Random();
        String randomTitle = String.valueOf(random.nextInt());
        mockMvc.perform(post("/api/problems/title")
                .contentType(MediaType.TEXT_PLAIN_VALUE).content(randomTitle))
                .andExpect(status().isOk());
        List<ProblemItem> problemList = requestGetProblems();
        assertTrue(problemList.stream().anyMatch(problemItem -> problemItem.title.equals(randomTitle)));
    }
}

