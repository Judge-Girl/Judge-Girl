package tw.waterball.judgegirl.springboot.submission.impl.mongo.strategy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.springboot.submission.impl.mongo.data.SubmissionData;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */

@ConditionalOnProperty(name = "judge-girl.submission-service.save-strategy",
        havingValue = VerdictShortcut.STRATEGY_NAME)
@Component
public class VerdictShortcut extends DuplicateDetectionCopyOnWrite {
    public static final String STRATEGY_NAME = "verdict-shortcut";

    public VerdictShortcut(MongoTemplate mongoTemplate, GridFsTemplate gridFsTemplate) {
        super(mongoTemplate, gridFsTemplate);
    }

    @Override
    protected void onDuplicate(SubmissionData newData, SubmissionData duplicateData) {
        newData.setVerdict(duplicateData.getVerdict());  // verdict shortcut
    }
}
