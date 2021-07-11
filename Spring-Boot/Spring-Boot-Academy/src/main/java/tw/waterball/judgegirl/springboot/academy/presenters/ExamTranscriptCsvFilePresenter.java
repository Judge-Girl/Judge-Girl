package tw.waterball.judgegirl.springboot.academy.presenters;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import tw.waterball.judgegirl.academy.domain.usecases.exam.CreateExamTranscriptUseCase;
import tw.waterball.judgegirl.academy.domain.usecases.exam.CreateExamTranscriptUseCase.ExamineeRecord;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * @author - c11037at@gmail.com (snowman)
 */
public class ExamTranscriptCsvFilePresenter implements CreateExamTranscriptUseCase.Presenter {
    private List<ProblemView> problems;
    private List<ExamineeRecord> examineeRecords;
    public static final String EXAM_TRANSCRIPT_FILE_NAME = "ExamTranscript.csv";

    @Override
    public void showExam(Exam exam) {
    }

    @Override
    public void showRecords(List<ExamineeRecord> examineeRecords) {
        this.examineeRecords = examineeRecords;
    }

    @Override
    public void showProblems(List<ProblemView> problems) {
        this.problems = problems;
    }

    private String[] createExamTranscriptCsvHeader(List<ProblemView> problemViews) {
        List<String> csvHeader = new ArrayList<>();
        csvHeader.add("Name");
        csvHeader.add("Email");
        for (ProblemView problemView : problemViews) {
            csvHeader.add(problemView.title);
        }
        csvHeader.add("Total Score");
        return csvHeader.toArray(new String[0]);
    }

    private List<List<String>> createExamTranscriptCsvBody(List<ExamineeRecord> examineeRecords) {
        return examineeRecords.stream().map(examineeRecord -> {
            List<String> row = new ArrayList<>();
            Student student = examineeRecord.getExaminee();
            row.add(student.getName());
            row.add(student.getEmail());
            List<CreateExamTranscriptUseCase.QuestionRecord> questionRecords = examineeRecord.getQuestionRecords();
            Map<Integer, Integer> questionScores = new HashMap<>();
            int totalScore = 0;
            for (CreateExamTranscriptUseCase.QuestionRecord questionRecord : questionRecords) {
                questionScores.put(questionRecord.getQuestion().getProblemId(), questionRecord.calculateScore());
            }
            for (ProblemView problem : problems) {
                int score = questionScores.get(problem.id);
                row.add(String.valueOf(score));
                totalScore += score;
            }
            row.add(String.valueOf(totalScore));
            return row;
        }).collect(toList());
    }

    public FileResource present() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            CSVPrinter csvPrinter = new CSVPrinter(
                    new PrintWriter(out),
                    CSVFormat.DEFAULT.withHeader(createExamTranscriptCsvHeader(problems))
            );

            List<List<String>> csvBody = createExamTranscriptCsvBody(examineeRecords);
            for (List<String> record : csvBody) {
                csvPrinter.printRecord(record);
            }
            csvPrinter.flush();

            ByteArrayInputStream inStream = new ByteArrayInputStream(out.toByteArray());
            return new FileResource(EXAM_TRANSCRIPT_FILE_NAME, out.size(), inStream);
        } catch (IOException e) {
            throw new RuntimeException("Create csv file of exam transcript error", e);
        }
    }
}
