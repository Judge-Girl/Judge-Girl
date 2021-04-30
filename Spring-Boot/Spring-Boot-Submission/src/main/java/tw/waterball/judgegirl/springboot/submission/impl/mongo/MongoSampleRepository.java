package tw.waterball.judgegirl.springboot.submission.impl.mongo;

import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.springboot.profiles.productions.Mongo;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.data.SampleSubmissionData;
import tw.waterball.judgegirl.submissionservice.domain.repositories.SampleRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author - wally55077@gmail.com
 */
@Mongo
@Component
@AllArgsConstructor
public class MongoSampleRepository implements SampleRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public void upgradeSubmissionToSample(int problemId, String... submissionIds) {
        Query query = Query.query(where("id").is(problemId));
        Object[] sampleSubmissionIds = Arrays.stream(submissionIds).toArray();
        Update update = new Update().push("samples").each(sampleSubmissionIds);
        mongoTemplate.upsert(query, update, SampleSubmissionData.class, COLLECTION_NAME);
    }

    @Override
    public List<String> findSampleSubmissionIds(int problemId) {
        Query query = Query.query(where("id").is(problemId));
        return Optional.ofNullable(mongoTemplate.findOne(query, SampleSubmissionData.class, COLLECTION_NAME))
                .map(SampleSubmissionData::getSamples)
                .orElseGet(ArrayList::new);
    }

    @Override
    public void downgradeSampleBackToSubmission(int problemId, String... submissionIds) {
        Query query = Query.query(where("id").is(problemId));
        Object[] sampleSubmissionIds = Arrays.stream(submissionIds).toArray();
        Update update = new Update().pullAll("samples", sampleSubmissionIds);
        mongoTemplate.updateFirst(query, update, SampleSubmissionData.class, COLLECTION_NAME);
    }

}
