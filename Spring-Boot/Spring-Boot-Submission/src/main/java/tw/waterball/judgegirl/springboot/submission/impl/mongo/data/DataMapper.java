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

package tw.waterball.judgegirl.springboot.submission.impl.mongo.data;

import tw.waterball.judgegirl.entities.submission.Report;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.entities.submission.Verdict;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A Data Mapper that maps Data Models to Entities and vice verse.
 * All mapping will return null given a null parameter.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public class DataMapper {

    public static List<Submission> toEntity(List<SubmissionData> dataList) {
        return dataList.stream().map(DataMapper::toEntity)
                .collect(Collectors.toList());
    }

    public static SubmissionData toData(Submission submission) {
        if (submission == null) {
            return null;
        }
        Verdict verdict = submission.getVerdict().orElse(null);
        return new SubmissionData(
                submission.getId(),
                submission.getProblemId(),
                submission.getLanguageEnvName(),
                submission.getStudentId(),
                toData(verdict),
                submission.getSubmittedCodesFileId(),
                submission.getSubmissionTime()
        );
    }

    public static VerdictData toData(Verdict verdict) {
        if (verdict == null) {
            return null;
        }
        return new VerdictData(
                verdict.getJudges(),
                verdict.getIssueTime(),
                verdict.getTotalGrade(),
                verdict.getSummaryStatus(),
                verdict.getCompileErrorMessage(),
                verdict.getReport().getRawData()
        );
    }

    public static Submission toEntity(SubmissionData data) {
        if (data == null) {
            return null;
        }
        Submission submission = new Submission(
                data.getId(),
                data.getStudentId(),
                data.getProblemId(),
                data.getLanguageEnvName(),
                data.getSubmittedCodesFileId(),
                data.getSubmissionTime()
        );

        Verdict verdict = toEntity(data.getVerdict());
        submission.setVerdict(verdict);
        return submission;
    }

    public static Verdict toEntity(VerdictData data) {
        if (data == null) {
            return null;
        }

        Verdict verdict = data.getCompileErrorMessage() == null ?
                new Verdict(data.getJudges(), data.getIssueTime()) :
                Verdict.compileError(data.getCompileErrorMessage(), data.getIssueTime());
        verdict.setReport(Report.fromData(data.getReportData()));
        return verdict;
    }
}
