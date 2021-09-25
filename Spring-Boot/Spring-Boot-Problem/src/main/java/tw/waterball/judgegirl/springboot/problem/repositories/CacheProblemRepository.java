package tw.waterball.judgegirl.springboot.problem.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.utils.functional.GetById;
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemQueryParams;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;
import tw.waterball.judgegirl.springboot.problem.repositories.data.ProblemData;

import java.io.InputStream;
import java.time.Duration;
import java.util.*;

import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;
import static tw.waterball.judgegirl.springboot.problem.repositories.data.ProblemData.toData;

/**
 * @author - wally55077@gmail.com
 */
@AllArgsConstructor
public class CacheProblemRepository implements ProblemRepository {

    public static final String PROBLEMS_PREFIX = "problems";
    private final ObjectMapper mapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final ProblemRepository problemRepository;

    @Override
    public Optional<FileResource> downloadTestCaseIOs(int problemId, String testcaseId) {
        return problemRepository.downloadTestCaseIOs(problemId, testcaseId);
    }

    @Override
    public Optional<Problem> findProblemById(int problemId) {
        return cacheProblemById(problemId, problemRepository::findProblemById);
    }

    @Override
    public Optional<FileResource> downloadProvidedCodes(int problemId, String languageEnvName) {
        return problemRepository.downloadProvidedCodes(problemId, languageEnvName);
    }

    @Override
    public List<Problem> find(ProblemQueryParams params) {
        return problemRepository.find(params);
    }

    @Override
    public List<Problem> findAll() {
        return problemRepository.findAll();
    }

    @Override
    public int getPageSize() {
        return problemRepository.getPageSize();
    }

    @Override
    public List<String> getTags() {
        return problemRepository.getTags();
    }

    @Override
    public Problem save(Problem problem, Map<LanguageEnv, InputStream> providedCodesZipMap) {
        return cacheProblem(problemRepository.save(problem, providedCodesZipMap));
    }

    @Override
    public Problem save(Problem problem) {
        return cacheProblem(problemRepository.save(problem));
    }

    @Override
    public void uploadProvidedCodes(Problem problem, Language language, List<FileResource> providedCodes) {
        invalidateProblemCache(problem.getId());
        problemRepository.uploadProvidedCodes(problem, language, providedCodes);
    }

    @Override
    public Problem patchTestcaseIOs(Problem problem, TestcaseIoPatching ioPatching) {
        invalidateProblemCache(problem.getId());
        return problemRepository.patchTestcaseIOs(problem, ioPatching);
    }

    @Override
    public void restoreProblem(Problem problem) {
        invalidateProblemCache(problem.getId());
        problemRepository.restoreProblem(problem);
    }

    @Override
    public boolean problemExists(int problemId) {
        return problemRepository.problemExists(problemId);
    }

    @Override
    public List<Problem> findProblemsByIds(int... problemIds) {
        return problemRepository.findProblemsByIds(problemIds);
    }

    @Override
    public void archiveProblem(Problem problem) {
        invalidateProblemCache(problem.getId());
        problemRepository.archiveProblem(problem);
    }

    @Override
    public void deleteProblem(Problem problem) {
        invalidateProblemCache(problem.getId());
        problemRepository.deleteProblem(problem);
    }

    @Override
    public void deleteAll() {
        redisTemplate.delete(getCacheProblemKeys());
        problemRepository.deleteAll();
    }

    @Override
    public void saveTags(List<String> tagList) {
        problemRepository.saveTags(tagList);
    }

    @Override
    public void deleteTestcaseById(int problemId, String testcaseId) {
        invalidateProblemCache(problemId);
        problemRepository.deleteTestcaseById(problemId, testcaseId);
    }

    private void invalidateProblemCache(int problemId) {
        redisTemplate.delete(getProblemKey(problemId));
    }

    private Optional<Problem> cacheProblemById(int problemId, GetById<Integer, Optional<Problem>> getActualProblemById) {
        var problemKey = getProblemKey(problemId);
        if (TRUE.equals(redisTemplate.hasKey(problemKey))) {
            return findProblemInCacheOrElse(problemId, getActualProblemById);
        } else {
            return getProblemFromDataBase(problemId, getActualProblemById);
        }
    }

    private Optional<Problem> findProblemInCacheOrElse(int problemId, GetById<Integer, Optional<Problem>> getActualProblemById) {
        var problemKey = getProblemKey(problemId);
        try {
            return ofNullable(mapper.readValue(redisTemplate.opsForValue().get(problemKey), ProblemData.class))
                    .map(ProblemData::toEntity);
        } catch (JsonProcessingException je) {
            redisTemplate.delete(problemKey);
            return getProblemFromDataBase(problemId, getActualProblemById);
        }
    }

    private Optional<Problem> getProblemFromDataBase(int problemId, GetById<Integer, Optional<Problem>> getActualProblemById) {
        Optional<Problem> problemOptional = getActualProblemById.get(problemId);
        problemOptional.ifPresent(this::cacheProblem);
        return problemOptional;
    }

    private Problem cacheProblem(Problem problem) {
        try {
            String problemKey = getProblemKey(problem.getId());
            redisTemplate.opsForValue()
                    .set(problemKey, mapper.writeValueAsString(toData(problem)), Duration.ofDays(1));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return problem;
    }

    private Collection<String> getCacheProblemKeys() {
        return ofNullable(redisTemplate.keys(PROBLEMS_PREFIX + ":*"))
                .orElseGet(Collections::emptySet);
    }

    private String getProblemKey(int problemId) {
        return String.format("%s:%d", PROBLEMS_PREFIX, problemId);
    }

}
