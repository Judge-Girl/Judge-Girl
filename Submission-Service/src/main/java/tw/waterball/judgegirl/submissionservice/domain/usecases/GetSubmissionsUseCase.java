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

package tw.waterball.judgegirl.submissionservice.domain.usecases;

import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.submissionservice.domain.repositories.SubmissionRepository;
import tw.waterball.judgegirl.submissionservice.domain.usecases.dto.SubmissionQueryParams;

import javax.inject.Named;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class GetSubmissionsUseCase {
    private SubmissionRepository submissionRepository;

    public GetSubmissionsUseCase(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    public void execute(SubmissionQueryParams queryParams, Presenter presenter) {
        List<Submission> submissions = submissionRepository.find(queryParams);
        presenter.setSubmissions(submissions);
    }

    public interface Presenter {
        void setSubmissions(List<Submission> submissions);
    }
}
