//package tw.waterball.judgegirl.migration.ci;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.SneakyThrows;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.boot.SpringApplication;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.gridfs.GridFsTemplate;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
//import tw.waterball.judgegirl.commons.models.files.FileResource;
//import tw.waterball.judgegirl.entities.problem.Language;
//import tw.waterball.judgegirl.judger.DefaultCCJudgerFactory;
//import tw.waterball.judgegirl.judger.Judger;
//import tw.waterball.judgegirl.migration.problem.ConvertLegacyLayout;
//import tw.waterball.judgegirl.migration.problem.MigrateOneProblem;
//import tw.waterball.judgegirl.migration.problem.NewJudgeGirlLayoutManipulator;
//import tw.waterball.judgegirl.migration.problem.PopulateOneProblem;
//import tw.waterball.judgegirl.plugins.impl.match.AllMatchPolicyPlugin;
//import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
//import tw.waterball.judgegirl.problemapi.views.ProblemView;
//import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemRepository;
//import tw.waterball.judgegirl.problemservice.domain.repositories.TestCaseRepository;
//import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
//import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
//import tw.waterball.judgegirl.submissionapi.views.VerdictIssuedEvent;
//import tw.waterball.judgegirl.submissionservice.domain.repositories.SubmissionRepository;
//import tw.waterball.judgegirl.submissionservice.domain.usecases.SubmitCodeRequest;
//
//import java.io.IOException;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//
//import static tw.waterball.judgegirl.migration.legacy.JudgeResultCodes.AC;
//
///**
// * @author - johnny850807@gmail.com (Waterball)
// */
//@RequiredArgsConstructor
//@Component
//public class MigrateOneProblemThenTest {
//    public static final Language DEFAULT_LANGUAGE = Language.C;
//
//    private final ObjectMapper objectMapper;
//    private final JdbcTemplate jdbcTemplate;
//    private final MongoTemplate mongoTemplate;
//    private final GridFsTemplate gridFsTemplate;
//    private final NewJudgeGirlLayoutManipulator layoutManipulator;
//    private final ProblemRepository problemRepository;
//    private final TestCaseRepository testcaseRepository;
//    private final SubmissionRepository submissionRepository;
//    private final List<String> submissionIds = new ArrayList<>(10);
//    private VerdictIssuedEvent result;
//
//    public void execute(MigrateOneProblem.Input input) throws Exception {
//        migrateOneProblem(input);
//        thenTest(input);
//    }
//
//    private void migrateOneProblem(MigrateOneProblem.Input input) throws Exception {
//        MigrateOneProblem migrateOneProblem =
//                new MigrateOneProblem(
//                        new ConvertLegacyLayout(input, objectMapper, jdbcTemplate),
//                        new PopulateOneProblem(input, mongoTemplate, gridFsTemplate, layoutManipulator));
//        migrateOneProblem.execute();
//    }
//
//    private void thenTest(MigrateOneProblem.Input input) {
//        int problemId = input.problemId();
//        var submissionIds = jdbcTemplate.query("select * from submissions where pid=? and res=?",
//                new Object[]{problemId, AC},
//                this::extractSubmissionIds);
//
//        if (submissionIds == null || submissionIds.isEmpty()) {
//            throw new IllegalStateException("No AC submissions found under the problem(id=" + problemId + ")");
//        }
//        submissionIds.forEach(submissionId ->
//                testSubmissionById(input.problemId(), submissionId));
//    }
//
//    @SneakyThrows
//    private void testSubmissionById(int problemId, String submissionId) {
//        Judger judger = DefaultCCJudgerFactory
//                .create("judger-layout.yaml",
//                        problemServiceDriver(),
//                        submissionServiceDriver(),
//                        result -> MigrateOneProblemThenTest.this.result = result,
//                        new AllMatchPolicyPlugin());
//
//        judger.judge(0, problemId, submissionId);
//
//    }
//
//    @NotNull
//    private SubmissionServiceDriver submissionServiceDriver() {
//        return new SubmissionServiceDriver() {
//            @Override
//            public SubmissionView submit(SubmitCodeRequest submitCodeRequest) throws IOException {
//                throw new IllegalStateException("Unsupported");
//            }
//            @Override
//            public SubmissionView getSubmission(int problemId, int studentId, String submissionId) throws NotFoundException {
//                return null;
//            }
//            @Override
//            public FileResource downloadSubmittedCodes(int problemId, int studentId, String submissionId, String submittedCodesFileId) throws NotFoundException {
//                return null;
//            }
//            @Override
//            public List<SubmissionView> getSubmissions(int problemId, int studentId) {
//                return null;
//            }
//        };
//    }
//
//    private ProblemServiceDriver problemServiceDriver() {
//        return new ProblemServiceDriver() {
//            @Override
//            public ProblemView getProblem(int problemId) throws NotFoundException {
//                return problemRepository.findProblemById(problemId)
//                        .map(ProblemView::fromEntity).orElseThrow();
//            }
//            @Override
//            public FileResource downloadProvidedCodes(int problemId, String languageEnvName, String providedCodesFileId) throws NotFoundException {
//                return problemRepository.downloadProvidedCodes(problemId, languageEnvName)
//                        .orElseThrow();
//            }
//            @Override
//            public FileResource downloadTestCaseIOs(int problemId, String testcaseIOsFileId) throws NotFoundException {
//                return testcaseRepository.downloadTestCaseIOs(problemId, testcaseIOsFileId)
//                        .orElseThrow();
//            }
//        };
//    }
//
//    @NotNull
//    private List<String> extractSubmissionIds(java.sql.ResultSet resultSet) throws SQLException {
//        List<String> ids = new LinkedList<>();
//        while (resultSet.next()) {
//            ids.add(resultSet.getString("sid"));
//        }
//        return ids;
//    }
//
//    public static void main(String[] args) throws Exception {
//        var context = SpringApplication.run(MigrateOneProblemThenTest.class);
//        var input = context.getBean(MigrateOneProblem.Input.class);
//        var migrateOneProblemThenTest = context.getBean(MigrateOneProblemThenTest.class);
//        migrateOneProblemThenTest.execute(input);
//    }
//
//}
