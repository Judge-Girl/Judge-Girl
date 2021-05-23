package tw.waterball.judgegirl.springboot.problem.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.redis.core.RedisTemplate;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problem.domain.repositories.PatchProblemParams;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemQueryParams;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author - wally55077@gmail.com
 */
@CacheConfig
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
        return problemRepository.findProblemById(problemId);
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
    }

    @Override
    public void updateProblemWithProvidedCodes(Problem problem, Language language, List<FileResource> providedCodes) {
        problemRepository.updateProblemWithProvidedCodes(problem, language, providedCodes);
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
    }

    @Override
    public void deleteProblem(Problem problem) {
        problemRepository.deleteProblem(problem);
    }

    @Override
    public void deleteAll() {
        Collection<String> keys = redisTemplate.keys(PROBLEMS_PREFIX + ":*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        problemRepository.deleteAll();
    }

    @Override
    public void saveTags(List<String> tagList) {
        problemRepository.saveTags(tagList);
    }
}
