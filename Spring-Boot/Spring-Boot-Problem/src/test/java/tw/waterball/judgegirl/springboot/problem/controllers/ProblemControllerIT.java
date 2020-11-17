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
package tw.waterball.judgegirl.springboot.problem.controllers;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tw.waterball.judgegirl.commons.utils.ZipUtils;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.problem.Testcase;
import tw.waterball.judgegirl.entities.stubs.Stubs;
import tw.waterball.judgegirl.problemapi.views.ProblemItem;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.springboot.problem.SpringBootProblemApplication;
import tw.waterball.judgegirl.springboot.problem.repositories.MongoProblemRepository;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.testkit.jupiter.ReplaceUnderscoresWithCamelCasesDisplayNameGenerators;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@SpringBootTest
@ActiveProfiles({Profiles.PROD, Profiles.MONGO})
@AutoConfigureDataMongo
@AutoConfigureMockMvc
@ContextConfiguration(classes = SpringBootProblemApplication.class)
@DisplayNameGeneration(ReplaceUnderscoresWithCamelCasesDisplayNameGenerators.class)
class ProblemControllerIT {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MongoClient mongoClient;
    private Problem problem;
    private List<Testcase> testcases;

    @BeforeEach
    void setup() {
        problem = Stubs.problemTemplateBuilder().build();
        testcases = asList(
                new Testcase("1", problem.getId(), 5, 5, 5000, 1, 20),
                new Testcase("2", problem.getId(), 5, 5, 5000, 1, 30),
                new Testcase("3", problem.getId(), 3, 4, 5000, 1, 50));
    }

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection(Problem.class);
        mongoTemplate.dropCollection(Testcase.class);
    }

    @Test
    void testDownloadZippedProvidedCodes() throws Exception {
        byte[] zippedProvidedCodesBytes =
                givenProblemWithProvidedCodes("/stubs/file1.c", "/stubs/file2.c");

        mockMvc.perform(get("/api/problems/{problemId}/providedCodes/{providedCodesFileId}",
                problem.getId(), problem.getProvidedCodesFileId()))
                .andExpect(status().isOk())
                .andExpect(header().longValue("Content-Length", zippedProvidedCodesBytes.length))
                .andExpect(content().contentType("application/zip"))
                .andExpect(content().bytes(zippedProvidedCodesBytes));
    }

    private byte[] givenProblemWithProvidedCodes(String... providedCodePaths) {
        final Problem savedProblem = Stubs.problemTemplateBuilder().build();
        byte[] zippedProvidedCodesBytes = ZipUtils.zipFilesFromResources(providedCodePaths);
        String fileId = gridFsTemplate.store(new ByteArrayInputStream(zippedProvidedCodesBytes),
                savedProblem.getProvidedCodesFileName()).toString();
        savedProblem.setProvidedCodesFileId(fileId);
        this.problem = mongoTemplate.save(savedProblem);
        return zippedProvidedCodesBytes;
    }

    @Test
    void GivenProblemSaved_WhenGetThatProblemById_ShouldRespondThatProblem() throws Exception {
        givenProblemSaved();

        mockMvc.perform(get("/api/problems/{problemId}", problem.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(
                        ProblemView.fromEntity(problem))));
    }

    private void givenProblemSaved() {
        final Problem problem = Stubs.problemTemplateBuilder().build();
        mongoTemplate.save(problem);
    }

    @Test
    void GivenTestcasesSaved_whenGetTestcasesByProblemId_shouldRespondTestCases() throws Exception {
        mongoTemplate.insertAll(testcases);

        mockMvc.perform(get("/api/problems/{problemId}/testcases", problem.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(testcases)));
    }

    @Test
    void testDownloadZippedTestCaseIOs() throws Exception {
        final Problem savedProblem = Stubs.problemTemplateBuilder().build();
        byte[] bytes = ZipUtils.zipFilesFromResources("/stubs/in/", "/stubs/out/");
        String fileId = gridFsTemplate.store(new ByteArrayInputStream(bytes),
                savedProblem.getTestCaseIOsFileName()).toString();
        savedProblem.setTestcaseIOsFileId(fileId);
        this.problem = mongoTemplate.save(savedProblem);

        mockMvc.perform(get("/api/problems/{problemId}/testcaseIOs/{testcaseIOsFileId}",
                savedProblem.getId(), problem.getTestcaseIOsFileId()))
                .andExpect(status().isOk())
                .andExpect(header().longValue("Content-Length", bytes.length))
                .andExpect(content().contentType("application/zip"))
                .andExpect(content().bytes(bytes));
    }

    @Test
    void GivenTagsSaved_WhenGetAllTags_ShouldRespondAllTags() throws Exception {
        final List<String> tags = asList("tag1", "tag2", "tag3");
        mongoTemplate.save(new MongoProblemRepository.AllTags(tags));

        mockMvc.perform(get("/api/problems/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(tags)));
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
        final Problem targetProblem = Stubs.problemTemplateBuilder().id(id)
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
        List<Problem> problems = givenArbitraryProblemsSaved(10);

        // verify all problems will be found and projected into problem-items
        assertEquals(problems.stream()
                .map(ProblemItem::fromEntity)
                .collect(Collectors.toList()), requestGetProblems());
    }

    @Test
    void GivenOneProblemSaved_WhenGetProblemsWithoutPageSpecified_ShouldRespondOnlyThatProblem() throws Exception {
        Problem expectedProblem = givenArbitraryProblemsSaved(1).get(0);

        assertEquals(ProblemItem.fromEntity(expectedProblem), requestGetProblems().get(0));
    }

    @Test
    void GivenManyProblemsSaved_WhenGetProblemsInPage_ShouldRespondOnlyThoseProblemsInThatPage() throws Exception {
        List<Problem> expectedProblems = givenArbitraryProblemsSaved(200);

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
                new TypeReference<List<ProblemItem>>() {
                });
    }

    private List<ProblemItem> requestGetProblemsInPage(int page) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/problems?page={page}", page))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<ProblemItem>>() {
                });
    }

    private List<Problem> givenArbitraryProblemsSaved(int count) {
        Random random = new Random();
        List<Problem> problems = IntStream.range(0, count).mapToObj((id) ->
                Stubs.problemTemplateBuilder().id(id)
                        .title(String.valueOf(random.nextInt())).build())
                .collect(Collectors.toList());
        problems.forEach(mongoTemplate::save);
        return problems;
    }
}

