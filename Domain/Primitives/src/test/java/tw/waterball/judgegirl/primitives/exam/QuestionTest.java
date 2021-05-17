package tw.waterball.judgegirl.primitives.exam;

import org.junit.jupiter.api.Test;
import tw.waterball.judgegirl.primitives.grading.Grading;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestionTest {

    @Test
    void testCalculateScore() {
        Question question = new Question(1, 1, 1, 50, 0);
        int score = question.calculateScore(new Grading() {
            @Override
            public int getGrade() {
                return 60;
            }

            @Override
            public int getMaxGrade() {
                return 100;
            }
        });
        assertEquals(30, score, "Grading 60/100 -> Question's Scoring 30/50");
    }
}