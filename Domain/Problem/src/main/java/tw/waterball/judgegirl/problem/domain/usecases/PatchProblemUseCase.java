package tw.waterball.judgegirl.problem.domain.usecases;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.problem.*;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static java.util.UUID.randomUUID;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToSet;

@Named
public class PatchProblemUseCase extends BaseProblemUseCase {
    public PatchProblemUseCase(ProblemRepository problemRepository) {
        super(problemRepository);
    }

    public void execute(Request request) throws NotFoundException {
        Problem problem = findProblem(request.problemId);
        request.patch(problem);
        problemRepository.save(problem);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        public static final int NOT_PRESENT = -1;
        public int problemId = NOT_PRESENT;
        public String title;
        public String description;
        public JudgePluginTagItem matchPolicyPluginTag;
        public Collection<JudgePluginTagItem> filterPluginTags;
        public LanguageEnvUpsert languageEnv;
        public TestcaseUpsert testcase;
        public Boolean visible;
        public List<String> tags;

        public void patch(Problem problem) {
            if (title != null) {
                problem.setTitle(title);
            }
            if (description != null) {
                problem.setDescription(description);
            }
            if (matchPolicyPluginTag != null) {
                problem.setOutputMatchPolicyPluginTag(matchPolicyPluginTag.toValue());
            }
            if (filterPluginTags != null) {
                problem.setFilterPluginTags(mapToSet(filterPluginTags, JudgePluginTagItem::toValue));
            }
            if (languageEnv != null) {
                LanguageEnv languageEnv = this.languageEnv.toValue();
                // providedCodes should be inherited from the old one
                problem.mayHaveLanguageEnv(languageEnv.getLanguage())
                        .ifPresent(l -> languageEnv.setProvidedCodesFileId(l.getProvidedCodesFileId()));
                problem.putLanguageEnv(languageEnv);
            }
            if (testcase != null) {
                problem.upsertTestcase(testcase.toValue());
            }
            if (visible != null) {
                problem.setVisible(visible);
            }
            if (tags != null) {
                problem.setTags(tags);
            }
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestcaseUpsert {
        public String id;
        public String name;
        public int problemId;
        public int timeLimit;
        public long memoryLimit;
        public long outputLimit;
        public int threadNumberLimit;
        public int grade;

        public TestcaseUpsert(String name, int problemId, int timeLimit, long memoryLimit, long outputLimit, int threadNumberLimit, int grade) {
            this(randomUUID().toString(), name, problemId, timeLimit, memoryLimit, outputLimit, threadNumberLimit, grade);
        }

        public static TestcaseUpsert upsert(Testcase testcase, Consumer<TestcaseUpsert> updating) {
            TestcaseUpsert testcaseUpsert = new TestcaseUpsert(testcase.getId(), testcase.getName(),
                    testcase.getProblemId(), testcase.getTimeLimit(),
                    testcase.getMemoryLimit(), testcase.getOutputLimit(),
                    testcase.getThreadNumberLimit(), testcase.getGrade());
            updating.accept(testcaseUpsert);
            return testcaseUpsert;
        }

        public Testcase toValue() {
            return new Testcase(id, name, problemId, timeLimit, memoryLimit, outputLimit, threadNumberLimit, grade);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LanguageEnvUpsert {
        public Language language;
        public String compilationScript;
        public float resourceSpecCpu;
        public float resourceSpecGpu;
        public List<SubmittedCodeSpecItem> submittedCodeSpecs;

        public static LanguageEnvUpsert upsert(LanguageEnv langEnv, Consumer<LanguageEnvUpsert> updating) {
            LanguageEnvUpsert update = fromLangEnv(langEnv);
            updating.accept(update);
            return update;
        }

        public static LanguageEnvUpsert fromLangEnv(LanguageEnv langEnv) {
            return new LanguageEnvUpsert(langEnv.getLanguage(),
                    langEnv.getCompilation().getScript(), langEnv.getResourceSpec().getCpu(),
                    langEnv.getResourceSpec().getGpu(), mapToList(langEnv.getSubmittedCodeSpecs(),
                    s -> new SubmittedCodeSpecItem(s.getFormat(), s.getFileName())));
        }

        public String getName() {
            return language.name();
        }

        public LanguageEnv toValue() {
            return LanguageEnv.builder()
                    .language(language)
                    .compilation(new Compilation(compilationScript))
                    .resourceSpec(new ResourceSpec(resourceSpecCpu, resourceSpecGpu))
                    .submittedCodeSpecs(mapToList(submittedCodeSpecs, SubmittedCodeSpecItem::toValue))
                    .build();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubmittedCodeSpecItem {
        public Language format;
        public String fileName;

        public SubmittedCodeSpec toValue() {
            return new SubmittedCodeSpec(format, fileName);
        }
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JudgePluginTagItem {
        public JudgePluginTag.Type type;
        public String group;
        public String name;
        public String version;

        public JudgePluginTagItem(JudgePluginTag tag) {
            this(tag.getType(), tag.getGroup(), tag.getName(), tag.getVersion());
        }

        public JudgePluginTag toValue() {
            return new JudgePluginTag(type, group, name, version);
        }
    }
}
