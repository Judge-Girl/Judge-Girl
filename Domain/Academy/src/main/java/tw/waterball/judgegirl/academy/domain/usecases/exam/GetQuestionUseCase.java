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
import lombok.Data;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

import javax.inject.Named;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Named
public class GetQuestionUseCase extends AbstractExamUseCase {
    private final ProblemServiceDriver problemService;

    public GetQuestionUseCase(ExamRepository examRepository, ProblemServiceDriver problemService) {
        super(examRepository);
        this.problemService = problemService;
    }

    public void execute(Request request, Presenter presenter) {
        Exam exam = findExam(request.examId);

        if (exam.containQuestion(request.problemId) && exam.hasExaminee(request.studentId)) {
            Problem problem = findNonArchivedProblem(request.problemId);
            presenter.showProblem(problem);
        } else {
            throw notFound(Problem.class).id(request.problemId);
        }
    }

    private Problem findNonArchivedProblem(int problemId) {
        return problemService.getProblem(problemId)
                .map(ProblemView::toEntity)
                .filter(p -> !p.isArchived())
                .orElseThrow(() -> notFound(Problem.class).id(problemId));
    }

    @Data
    @AllArgsConstructor
    public static class Request {
        private int examId;
        private int problemId;
        private int studentId;
    }

    public interface Presenter {
        void showProblem(Problem problem);
    }
}
