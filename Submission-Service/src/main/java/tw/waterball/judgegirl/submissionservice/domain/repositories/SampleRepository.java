package tw.waterball.judgegirl.submissionservice.domain.repositories;

import java.util.List;

/**
 * @author - wally55077@gmail.com
 */
public interface SampleRepository {

    String COLLECTION_NAME = "sample";

    void upgradeSubmissionsToSamples(int problemId, String... submissionIds);

    List<String> findSampleSubmissionIds(int problemId);

    void downgradeSamplesBackToSubmissions(int problemId, String... submissionIds);

}
