package tw.waterball.judgegirl.springboot.submission.impl.mongo.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedList;
import java.util.List;

import static tw.waterball.judgegirl.submission.domain.repositories.SampleRepository.COLLECTION_NAME;

/**
 * @author - wally55077@gmail.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(COLLECTION_NAME)
public class SampleSubmissionData {

    @Id
    private String id;

    private List<String> samples = new LinkedList<>();

}
