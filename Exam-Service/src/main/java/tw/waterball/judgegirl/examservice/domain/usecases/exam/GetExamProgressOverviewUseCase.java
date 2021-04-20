package tw.waterball.judgegirl.examservice.domain.usecases.exam;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.exam.Exam;
import tw.waterball.judgegirl.entities.exam.Question;
import tw.waterball.judgegirl.entities.exam.Record;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.examservice.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.studentapi.clients.ProblemServiceDriver;

import javax.inject.Named;
import java.util.Optional;

import static tw.waterball.judgegirl.problemapi.views.ProblemView.toEntity;

@Named
@AllArgsConstructor
public class GetExamProgressOverviewUseCase {
    private final ExamRepository examRepository;
    private final ProblemServiceDriver problemService;

    public void execute(Request request, Presenter presenter) {
        Exam exam = findExam(request);
        showExam(presenter, exam);
        showEachQuestion(presenter, exam);
        showBestRecordOfEachQuestionTheStudentAchieved(request, presenter, exam);
        showRemainingQuotaOfEachQuestion(request.studentId, exam, presenter);
    }

    private Exam findExam(Request request) {
        return examRepository.findById(request.examId).orElseThrow(NotFoundException::new);
    }

    private void showExam(Presenter presenter, Exam exam) {
        presenter.showExam(exam);
    }

    private void showEachQuestion(Presenter presenter, Exam exam) {
        // TODO: should be improved to fetch all the problems in one query
        for (Question question : exam.getQuestions()) {
            Problem problem = toEntity(problemService.getProblem(question.getId().getProblemId()));
            presenter.showQuestion(question, problem);
        }
    }

    private void showBestRecordOfEachQuestionTheStudentAchieved(Request request, Presenter presenter, Exam exam) {
        exam.getQuestions().forEach(question ->
                examRepository.findBestRecordOfQuestion(question.getId(), request.studentId)
                        .ifPresent(presenter::showBestRecordOfQuestion));
    }

    private void showRemainingQuotaOfEachQuestion(int studentId, Exam exam, Presenter presenter) {
        exam.getQuestions().forEach(question -> {
            int answerCount = examRepository.countAnswersInQuestion(question.getId(), studentId);
            presenter.showRemainingQuotaOfQuestion(question, question.getQuota() - answerCount);
        });
    }

    public interface Presenter {
        void showExam(Exam exam);

        void showQuestion(Question question, Problem problem);

        void showBestRecordOfQuestion(Record bestRecord);

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
