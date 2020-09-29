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
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.submissionservice.domain.usecases.SubmissionRequest;

import java.io.IOException;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface SubmissionServiceDriver {

    Submission submit(String token, SubmissionRequest submissionRequest) throws IOException;

    Submission getSubmission(String token, int problemId, int studentId, String submissionId) throws NotFoundException;

    FileResource getZippedSubmittedCodes(String token, int problemId, int studentId, String submissionId) throws NotFoundException;

    List<Submission> getSubmissions(String token, int problemId, int studentId);

}
