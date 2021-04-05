package tw.waterball.judgegirl.springboot.submission.impl.mongo.strategy;

import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.entities.submission.Submission;

import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface SaveSubmissionWithCodesStrategy {
    Submission perform(Submission submission, List<FileResource> originalCodes, String fileName);
}
