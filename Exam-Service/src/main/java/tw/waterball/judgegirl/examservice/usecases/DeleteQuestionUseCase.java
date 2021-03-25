package tw.waterball.judgegirl.examservice.usecases;

import lombok.AllArgsConstructor;
import lombok.Value;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.entities.Question;
import tw.waterball.judgegirl.examservice.repositories.ExamRepository;

import javax.inject.Named;

@Named
@AllArgsConstructor
public class DeleteQuestionUseCase {
    private final ExamRepository examRepository;

    public void execute(Request request) throws NotFoundException {
        Exam exam = examRepository.findById(request.examId).orElseThrow(NotFoundException::new);
        if (!exam.getQuestions().removeIf(question -> question.getId().equals(new Question.QuestionId(request.examId,request.problemId)))) {
            throw new NotFoundException();
        }
        examRepository.save(exam);
    }

    @Value
    public static class Request {
        public int examId;
        public int problemId;
    }
}
