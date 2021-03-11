package tw.waterball.judgegirl.migration.submission;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.models.files.StreamingResource;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.entities.problem.Language;
import tw.waterball.judgegirl.entities.submission.Judge;
import tw.waterball.judgegirl.entities.submission.ProgramProfile;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.entities.submission.Verdict;
import tw.waterball.judgegirl.migration.legacy.JudgeResultCodes;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.submissionservice.domain.repositories.SubmissionRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static tw.waterball.judgegirl.commons.utils.ZipUtils.zipToFile;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Component
@SpringBootApplication(scanBasePackages = "tw.waterball.judgegirl")
public class MigrateSubmissions {
    public static final Language DEFAULT_LANG = Language.C;
    private final JdbcTemplate jdbcTemplate;
    private final SubmissionRepository submissionRepository;
    public final Path submissionsPath;

    public MigrateSubmissions(JdbcTemplate jdbcTemplate,
                              SubmissionRepository submissionRepository, @Value("${submissionsPath}") String submissionsPath) {
        this.jdbcTemplate = jdbcTemplate;
        this.submissionRepository = submissionRepository;
        this.submissionsPath = Paths.get(submissionsPath);
    }

    public static void main(String[] args) {
        var context = SpringApplication.run(MigrateSubmissions.class);
        try {
            MigrateSubmissions migrateSubmissions = context.getBean(MigrateSubmissions.class);
            migrateSubmissions.execute();
            SpringApplication.exit(context, () -> 1);
            System.out.println("Completed");
        } catch (Exception err) {
            err.printStackTrace();
            SpringApplication.exit(context, () -> -1);
        }
        ;
    }

    @SneakyThrows
    public void execute() {
        int retry = 30;
        List<String> problemIds = jdbcTemplate.query("select pid from problems", (resultSet, i) -> resultSet.getString(1));
        for (String problemId : problemIds) {
            var acSubmissions = jdbcTemplate.query("select * from submissions where pid = ? and res = ? limit 10",
                    new Object[]{problemId, JudgeResultCodes.AC}, this::extractOldSubmissions);
            if (acSubmissions.isEmpty()) {
                System.out.println("No submissions : problem[id=" + problemId + "]");
            } else {
                for (int i = 0; i < Math.min(30, acSubmissions.size()); i++) {
                    OldSubmission oldACSubmission = acSubmissions.get(i);
                    try {
                        System.out.println(problemId);
                        Submission submission = convertSubmission(oldACSubmission);
                        saveSubmission(submission);
                        break;
                    } catch (Exception err) {
                        // Malformed submitted codes --> skip
                        err.printStackTrace();
                        System.out.println("Retry on the next AC submission.");
                    }
                }
            }
        }
    }

    private void saveSubmission(Submission submission) {
        Submission saved = submissionRepository.save(submission);
        System.out.println(SubmissionView.fromEntity(saved));
    }

    private Submission convertSubmission(OldSubmission oldSubmission) throws IOException {
        String verdictScript;
        String submittedCodesFileId = extractSubmittedCodesAndSave(oldSubmission);
        verdictScript = Files.readString(submissionsPath.resolve(oldSubmission.sid + "-z"));
        Submission submission = new Submission(String.valueOf(oldSubmission.sid), oldSubmission.uid,
                oldSubmission.pid, DEFAULT_LANG.toString(), submittedCodesFileId, new Date(oldSubmission.ts));
        submission.setVerdict(evaluateVerdict(oldSubmission.ts, verdictScript));
        return submission;
    }

    private String extractSubmittedCodesAndSave(OldSubmission oldSubmission) throws IOException {
        Set<File> files = new HashSet<>();
        int i = 0;
        do {
            File file = submissionsPath.resolve(oldSubmission.sid + "-" + i).toFile();
            assert file.exists();
            files.add(file);
            i++;
        } while (Files.exists(submissionsPath.resolve(oldSubmission.sid + "-" + i)));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        zipToFile(files.toArray(new File[0]), baos);
        String fileName = String.format("%d-%s-%d.zip", oldSubmission.sid, oldSubmission.pid,
                oldSubmission.ts);
        return saveZippedSubmittedCodeAndGetFileId(
                new StreamingResource(fileName, new ByteArrayInputStream(baos.toByteArray())));
    }

    private String saveZippedSubmittedCodeAndGetFileId(StreamingResource streamingResource) throws IOException {
        return submissionRepository.saveZippedSubmittedCodesAndGetFileId(streamingResource);
    }

    /**
     * Subtask (5pt)
     * <p>
     * 19.in
     * cpu 843 ms
     * mem 131072 B
     * Accepted
     * <p>
     * Subtask (5pt)
     * <p>
     * 20.in
     * cpu 716 ms
     * mem 131072 B
     * Accepted
     */
    private static Verdict evaluateVerdict(long time, String verdictScript) {
        verdictScript = verdictScript.trim();
        List<Judge> judges = new ArrayList<>();
        String[] lines = verdictScript.split("\n");
        if (!lines[0].contains("Subtask")) {
            // CE
            return new Verdict(verdictScript);
        }
        int i = 0;
        while (i + 5 < lines.length) {
            if (lines[i].startsWith("Subtask")) {
                int grade = parseInt(lines[i].split("([(p])")[1]);
                String testcaseName = lines[i + 2];
                long cpuTime = parseLong(lines[i + 3].split(" ")[1]);
                long mem = parseLong(lines[i + 4].split(" ")[1]);
                String status = lines[i + 5]; //
                JudgeStatus judgeStatus = parseJudgeStatus(status);
                Judge judge = new Judge(testcaseName, judgeStatus,
                        new ProgramProfile(cpuTime, mem, ""),
                        judgeStatus == JudgeStatus.AC ? grade : 0);
                judges.add(judge);
                i += 7;
            } else {
                throw new IllegalStateException("Unexpected");
            }
        }

        return new Verdict(judges, new Date(time));
    }

    private static JudgeStatus parseJudgeStatus(String status) {
        switch (status) {
            case "Accepted":
                return JudgeStatus.AC;
            case "Wrong Answer":
                return JudgeStatus.WA;
            case "Runtime Error":
                return JudgeStatus.RE;
            case "Time Limit Exceeded":
                return JudgeStatus.TLE;
            case "Memory Limit Exceeded":
                return JudgeStatus.MLE;
            case "Compile Error":
                return JudgeStatus.CE;
            case "Output Limit Exceeded":
                return JudgeStatus.OLE;
            case "Presentation Error":
                return JudgeStatus.PE;
            default:
                throw new IllegalStateException("Unexpected status: " + status);
        }

    }

    @NotNull
    private List<OldSubmission> extractOldSubmissions(java.sql.ResultSet resultSet) throws SQLException {
        List<OldSubmission> s = new LinkedList<>();
        while (resultSet.next()) {
            OldSubmission oldSubmission =
                    new OldSubmission(resultSet.getInt("sid"),
                            resultSet.getInt("uid"),
                            resultSet.getInt("pid"),
                            resultSet.getInt("cid"),
                            resultSet.getLong("ts"),
                            resultSet.getInt("lng"),
                            resultSet.getInt("len"),
                            resultSet.getInt("scr"),
                            resultSet.getInt("res"),
                            resultSet.getInt("cpu"),
                            resultSet.getInt("mem"));
            s.add(oldSubmission);
        }
        return s;
    }

    @AllArgsConstructor
    public static class OldSubmission {
        public int sid;
        public int uid;
        public int pid;
        public int cid;
        public long ts;
        public int lng;
        public int len;
        public int scr;
        public int res;
        public int cpu;
        public int mem;
    }
}
