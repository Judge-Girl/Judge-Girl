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
import tw.waterball.judgegirl.entities.problem.TestCase;
import tw.waterball.judgegirl.problemapi.views.ProblemItem;
import tw.waterball.judgegirl.springboot.problem.SpringBootProblemApplication;
import tw.waterball.judgegirl.springboot.problem.repositories.MongoProblemAndTestCaseRepository;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.testkit.jupiter.ReplaceUnderscoresWithCamelCasesDisplayNameGenerator;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static tw.waterball.judgegirl.problemapi.views.ProblemItem.project;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@SpringBootTest
@ActiveProfiles({Profiles.PROD, Profiles.MONGO})
@AutoConfigureDataMongo
@AutoConfigureMockMvc
@ContextConfiguration(classes = SpringBootProblemApplication.class)
@DisplayNameGeneration(ReplaceUnderscoresWithCamelCasesDisplayNameGenerator.class)
class ProblemControllerTest {
    Problem problem;
    List<TestCase> testCases;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        problem = Stubs.PROBLEM_TEMPLATE_BUILDER.build();
        testCases = asList(
                new TestCase("1", problem.getId(), 5, 5, 5000, 1, 20),
                new TestCase("2", problem.getId(), 5, 5, 5000, 1, 30),
                new TestCase("3", problem.getId(), 3, 4, 5000, 1, 50));
    }

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection(Problem.class);
        mongoTemplate.dropCollection(TestCase.class);
    }

    @Test
    void testDownloadZippedProvidedCodes() throws Exception {
        byte[] zippedProvidedCodesBytes =
                givenProblemWithProvidedCodes("/stubs/file1.c", "/stubs/file2.c");

        mockMvc.perform(get("/api/problems/{problemId}/zippedProvidedCodes", problem.getId()))
                .andExpect(status().isOk())
                .andExpect(header().longValue("Content-Length", zippedProvidedCodesBytes.length))
                .andExpect(content().contentType("application/zip"))
                .andExpect(content().bytes(zippedProvidedCodesBytes));
    }

    private byte[] givenProblemWithProvidedCodes(String... providedCodePaths) {
        final Problem problem = Stubs.PROBLEM_TEMPLATE_BUILDER.build();
        byte[] zippedProvidedCodesBytes = ZipUtils.zipClassPathResources(providedCodePaths);
        String fileId = gridFsTemplate.store(new ByteArrayInputStream(zippedProvidedCodesBytes),
                problem.getZippedProvidedCodesFileName()).toString();
        problem.setZippedProvidedCodesFileId(fileId);
        mongoTemplate.save(problem);
        return zippedProvidedCodesBytes;
    }

    @Test
    void GivenProblemSaved_WhenGetThatProblemById_ShouldReturnThatProblem() throws Exception {
        givenProblemSaved();

        mockMvc.perform(get("/api/problems/{problemId}", problem.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(problem)));
    }

    private void givenProblemSaved() {
        final Problem problem = Stubs.PROBLEM_TEMPLATE_BUILDER.build();
        mongoTemplate.save(problem);
    }

    @Test
    void getTestCases() throws Exception {
        mongoTemplate.insertAll(testCases);

        mockMvc.perform(get("/api/problems/{problemId}/testcases", problem.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(testCases)));
    }

    private void givenTestCases() {
        mongoTemplate.insertAll(testCases);
    }

    @Test
    void getZippedTestCaseInputs() throws Exception {
        final Problem problem = Stubs.PROBLEM_TEMPLATE_BUILDER.build();
        byte[] bytes = ZipUtils.zipClassPathResources("/stubs/I1.in", "/stubs/I2.in", "/stubs/I3.in");
        String fileId = gridFsTemplate.store(new ByteArrayInputStream(bytes),
                problem.getZippedTestCaseIOsFileName()).toString();
        problem.setZippedTestCaseInputsFileId(fileId);
        mongoTemplate.save(problem);

        mockMvc.perform(get("/api/problems/{problemId}/zippedTestCaseInputs", problem.getId()))
                .andExpect(status().isOk())
                .andExpect(header().longValue("Content-Length", bytes.length))
                .andExpect(content().contentType("application/zip"))
                .andExpect(content().bytes(bytes));
    }

    @Test
    void getZippedTestCaseOutputs() throws Exception {
        final Problem problem = Stubs.PROBLEM_TEMPLATE_BUILDER.build();
        byte[] bytes = ZipUtils.zipClassPathResources("/stubs/O1.out", "/stubs/O2.out", "/stubs/O3.out");
        String fileId = gridFsTemplate.store(new ByteArrayInputStream(bytes),
                problem.getZippedTestCaseOutputsFileName()).toString();
        problem.setZippedTestCaseOutputsFileId(fileId);
        mongoTemplate.save(problem);

        mockMvc.perform(get("/api/problems/{problemId}/zippedTestCaseOutputs", problem.getId()))
                .andExpect(status().isOk())
                .andExpect(header().longValue("Content-Length", bytes.length))
                .andExpect(content().contentType("application/zip"))
                .andExpect(content().bytes(bytes));
    }

    @Test
    void GivenTagsSaved_WhenGetAllTags_ShouldReturnAllTags() throws Exception {
        final List<String> tags = asList("tag1", "tag2", "tag3");
        mongoTemplate.save(new MongoProblemAndTestCaseRepository.AllTags(tags));

        mockMvc.perform(get("/api/problems/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(tags)));
    }

    @Test
    void GivenTaggedProblemsSaved_WhenGetProblemsByMatchingTags_ShouldReturnThoseProblemItems() throws Exception {
        final ProblemItem targetProblem1 = project(givenProblemWithTags(1, "tag1", "tag2"));
        final ProblemItem targetProblem2 = project(givenProblemWithTags(2, "tag1", "tag2"));

        verifyFindProblemsByTagsWillGet(asList("tag1", "tag2"), asList(targetProblem1, targetProblem2));
        verifyFindProblemsByTagsWillGet(singletonList("tag1"), asList(targetProblem1, targetProblem2));
        verifyFindProblemsByTagsWillGet(singletonList("tag2"), asList(targetProblem1, targetProblem2));
    }

    @Test
    void GivenTaggedProblemsSaved_WhenGetProblemsByNonMatchingTags_ShouldReturnEmptyArray() throws Exception {
        project(givenProblemWithTags(1, "tag1", "tag2"));
        project(givenProblemWithTags(2, "tag1", "tag2"));

        verifyFindProblemsByTagsWillGet(asList("tag1", "tag2", "tag3"), emptyList());
        verifyFindProblemsByTagsWillGet(singletonList("Non-existent-tag"), emptyList());
    }

    private Problem givenProblemWithTags(int id, String... tags) {
        final Problem targetProblem = Stubs.PROBLEM_TEMPLATE_BUILDER.id(id)
                .tags(asList(tags)).build();
        mongoTemplate.save(targetProblem);
        return targetProblem;
    }

    private void verifyFindProblemsByTagsWillGet(List<String> tags, List<ProblemItem> problemItems) throws Exception {
        String tagsSplitByCommas = String.join(", ", tags);

        mockMvc.perform(get("/api/problems?tags={tags}", tagsSplitByCommas))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(problemItems)));
    }

    @Test
    void GivenProblemsSaved_WhenGetAllProblems_ShouldReturnAll() throws Exception {
        List<Problem> problems = givenArbitraryProblemsSaved(10);


        // verify all problems will be found and projected into problem-items
        assertEquals(problems.stream()
                .map(ProblemItem::project)
                .collect(Collectors.toList()), getProblems());
    }

    @Test
    void GivenOneProblemSaved_WhenGetProblemsWithoutSpecifyingPage_ShouldReturnThatProblem() throws Exception {
        Problem expectedProblem = givenArbitraryProblemsSaved(1).get(0);

        assertEquals(project(expectedProblem), getProblems().get(0));
    }

    @Test
    void GivenManyProblemsSaved_WhenGetProblemsInPage_ShouldReturnOnlyThoseProblemsInThatPage() throws Exception {
        List<Problem> expectedProblems = givenArbitraryProblemsSaved(200);

        int page = 0;
        List<ProblemItem> actualAllProblemItems = new ArrayList<>();
        Set<ProblemItem> actualProblemItemsInPreviousPage = new HashSet<>();
        List<ProblemItem> actualProblemItems;

        do {
            actualProblemItems = getProblemsByPage(page);
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

    private List<ProblemItem> getProblems() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/problems"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<ProblemItem>>() {
                });
    }

    private List<ProblemItem> getProblemsByPage(int page) throws Exception {
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
                Stubs.PROBLEM_TEMPLATE_BUILDER.id(id)
                        .title(String.valueOf(random.nextInt())).build()).collect(Collectors.toList());
        problems.forEach(mongoTemplate::save);
        return problems;
    }
}

