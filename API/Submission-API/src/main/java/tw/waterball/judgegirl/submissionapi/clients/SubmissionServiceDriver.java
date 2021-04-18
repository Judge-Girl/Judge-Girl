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

package tw.waterball.judgegirl.submissionapi.clients;


import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.entities.submission.SubmissionThrottlingException;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface SubmissionServiceDriver {

    SubmissionView submit(SubmitCodeRequest submitCodeRequest) throws SubmissionThrottlingException;

    SubmissionView getSubmission(int problemId, int studentId, String submissionId) throws NotFoundException;

    FileResource downloadSubmittedCodes(int problemId, int studentId, String submissionId, String submittedCodesFileId) throws NotFoundException;

    default List<SubmissionView> getSubmissions(int problemId, int studentId) {
        return getSubmissions(problemId, studentId, emptyMap());
    }

    List<SubmissionView> getSubmissions(int problemId, int studentId, Map<String, String> bagQueryParameters);

    SubmissionView findBestRecord(List<String> submissionIds);

    SubmissionView findBestRecord(int problemId, int studentId) throws NotFoundException;


}
