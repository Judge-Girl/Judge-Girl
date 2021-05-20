package tw.waterball.judgegirl.springboot.problem.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.RedisTemplate;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problem.domain.repositories.PatchProblemParams;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemQueryParams;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;

import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

import static java.lang.Boolean.TRUE;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

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
    public Optional<FileResource> downloadTestCaseIOs(int problemId, String testcaseIOsFileId) {
        return problemRepository.downloadTestCaseIOs(problemId, testcaseIOsFileId);
    }

    @Override
    public Optional<Problem> findProblemById(int problemId) {
        return findCacheProblem(problemId, () -> problemRepository.findProblemById(problemId));
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
    public Problem save(Problem problem, Map<LanguageEnv, InputStream> providedCodesZipMap, InputStream testcaseIOsZip) {
        return problemRepository.save(problem, providedCodesZipMap, testcaseIOsZip);
    }

    @SneakyThrows
    @Override
    public Problem save(Problem problem) {
        return problemRepository.save(problem);
    }

    @Override
    public void patchProblem(int problemId, PatchProblemParams params) {
        problemRepository.patchProblem(problemId, params);
        redisTemplate.delete(getProblemKey(String.valueOf(problemId)));
    }

    @Override
    public void updateProblemWithProvidedCodes(Problem problem, Language language, List<FileResource> providedCodes) {
        problemRepository.updateProblemWithProvidedCodes(problem, language, providedCodes);
        redisTemplate.delete(getProblemKey(String.valueOf(problem.getId())));
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
        problemRepository.archiveProblem(problem);
        redisTemplate.delete(getProblemKey(String.valueOf(problem.getId())));
    }

    @Override
    public void deleteProblem(Problem problem) {
        problemRepository.deleteProblem(problem);
        redisTemplate.delete(getProblemKey(String.valueOf(problem.getId())));
    }

    @Override
    public void deleteAll() {
        problemRepository.deleteAll();
        redisTemplate.delete(getCacheProblemKeys());
    }

    @Override
    public void saveTags(List<String> tagList) {
        problemRepository.saveTags(tagList);
    }

    private Optional<Problem> findCacheProblem(int problemId, Supplier<Optional<Problem>> problemSupplier) {
        var problemKey = getProblemKey(String.valueOf(problemId));
        if (TRUE.equals(redisTemplate.hasKey(problemKey))) {
            return toProblemOptional(problemKey);
        } else {
            Optional<Problem> problemOptional = problemSupplier.get();
            problemOptional.ifPresent(this::cacheProblem);
            return problemOptional;
        }
    }

    private Optional<Problem> toProblemOptional(String problemKey) {
        try {
            return ofNullable(mapper.readValue(redisTemplate.opsForValue().get(problemKey), Problem.class));
        } catch (Exception e) {
            return empty();
        }
    }

    private void cacheProblem(Problem problem) {
        try {
            String problemKey = getProblemKey(String.valueOf(problem.getId()));
            redisTemplate.opsForValue()
                    .set(problemKey, mapper.writeValueAsString(problem), Duration.ofDays(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Collection<String> getCacheProblemKeys() {
        return ofNullable(redisTemplate.keys(getProblemKey("*")))
                .orElseGet(Collections::emptySet);
    }

    private String getProblemKey(String id) {
        return PROBLEMS_PREFIX + ":" + id;
    }

}
