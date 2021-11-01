/*
 * Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.Examinee;
import tw.waterball.judgegirl.primitives.exam.Question;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

import javax.inject.Named;
import java.util.Optional;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Named
@AllArgsConstructor
public class GetExamProblemUseCase {
    private final ExamRepository examRepository;
    private final ProblemServiceDriver problemService;

    public void execute(int examId, int problemId, int studentId, Presenter presenter) {
        Exam exam = findExam(examId);
        Problem problem = findProblem(problemId);
        Optional<Question> question = exam.getQuestionByProblemId(problemId);
        Optional<Examinee> examinee = exam.getExaminee(studentId);

        if (question.isPresent() && examinee.isPresent() && !problem.isArchived()) {
            presenter.showProblem(problem);
        } else {
            throw notFound(Problem.class).id(problemId);
        }
    }

    private Exam findExam(int examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> notFound(Exam.class).id(examId));
    }

    private Problem findProblem(int problemId) {
        return problemService.getProblem(problemId)
                .map(ProblemView::toEntity)
                .orElseThrow(() -> notFound(Problem.class).id(problemId));
    }

    public interface Presenter {
        void showProblem(Problem problem);
    }
}
