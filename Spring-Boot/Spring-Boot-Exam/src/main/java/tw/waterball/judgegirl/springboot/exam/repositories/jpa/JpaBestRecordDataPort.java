package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import org.springframework.data.repository.CrudRepository;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface JpaBestRecordDataPort extends CrudRepository<BestRecordData, BestRecordData.Id> {
}
