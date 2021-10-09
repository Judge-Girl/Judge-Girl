package tw.waterball.judgegirl.primitives.exam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tw.waterball.judgegirl.primitives.time.Duration;

import java.util.Date;

import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class ExamTest {

    private Exam exam;

    @BeforeEach
    public void beforeEach() {
        exam = new Exam(1, "name", Duration.during(new Date(), new Date()), "description");
    }

    @Test
    public void givenThreeQuestions_ABC_whenReorderWith_213_requestShouldBeBAC() {
        int examId = exam.getId();
        Question A = new Question(examId, 1, 100, 100, 1);
        Question B = new Question(examId, 2, 100, 100, 3);
        Question C = new Question(examId, 3, 100, 100, 3);
        exam.addQuestion(A);
        exam.addQuestion(B);
        exam.addQuestion(C);
        var questions = exam.getQuestions();
        assertEquals(3, questions.size());

        exam.reorderQuestions(2, 1, 3);

        questions.sort(comparing(Question::getQuestionOrder));

        assertEquals(questions.get(0), B);
        assertEquals(questions.get(1), A);
        assertEquals(questions.get(2), C);
    }

    @Test
    public void givenTwoQuestions_AB_whenReorderWith_213_shouldFailed() {
        int examId = exam.getId();
        Question A = new Question(examId, 1, 100, 100, 1);
        Question B = new Question(examId, 2, 100, 100, 2);
        exam.addQuestion(A);
        exam.addQuestion(B);
        var questions = exam.getQuestions();
        assertEquals(2, questions.size());

        assertThrows(IllegalArgumentException.class, () -> exam.reorderQuestions(2, 1, 3));
    }
}