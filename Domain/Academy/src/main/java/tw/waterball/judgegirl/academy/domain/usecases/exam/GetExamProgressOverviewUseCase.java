package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.Question;
import tw.waterball.judgegirl.primitives.exam.Record;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;

import javax.inject.Named;
import javax.validation.Valid;
import java.util.Optional;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;
import static tw.waterball.judgegirl.problemapi.views.ProblemView.toEntity;

@Named
@AllArgsConstructor
public class GetExamProgressOverviewUseCase {
    private final ExamRepository examRepository;
    private final ProblemServiceDriver problemService;

    public void execute(Request request, Presenter presenter) {
        int studentId = request.studentId;
        Exam exam = findExam(request);
        presenter.showExam(exam);

        exam.foreachQuestion(question -> {
            Problem problem = findProblem(question);
            presenter.showQuestion(question, problem);
            showRemainingQuotaOfQuestion(presenter, studentId, question);
            findBestRecord(studentId, question)
                    .ifPresentOrElse(record -> {
                        int yourScore = question.calculateScore(record);
                        presenter.showBestRecordOfQuestion(record);
                        presenter.showYourScoreOfQuestion(question, yourScore);
                    }, () -> presenter.showYourScoreOfQuestion(question, 0));
        });
    }

    private Exam findExam(Request request) {
        return examRepository.findById(request.examId)
                .orElseThrow(() -> notFound(Exam.class).id(request.examId));
    }

    private Problem findProblem(Question question) {
        return toEntity(problemService.getProblem(question.getId().getProblemId()));
    }

    private Optional<Record> findBestRecord(int studentId, @Valid Question question) {
        return examRepository.findRecordOfQuestion(question.getId(), studentId);
    }

    private void showRemainingQuotaOfQuestion(Presenter presenter, int studentId, Question question) {
        int answerCount = examRepository.countAnswersInQuestion(question.getId(), studentId);
        presenter.showRemainingQuotaOfQuestion(question, question.getQuota() - answerCount);
    }

    public interface Presenter {
        void showExam(Exam exam);

        void showQuestion(Question question, Problem problem);

        void showBestRecordOfQuestion(Record bestRecord);

        void showYourScoreOfQuestion(Question question, int yourScore);

        void showRemainingQuotaOfQuestion(Question question, int remainingQuota);
    }

    @AllArgsConstructor
    public static class Request {
        public int examId;
        public Integer studentId;

        public Optional<Integer> getStudentId() {
            return Optional.of(studentId);
        }
    }
}
