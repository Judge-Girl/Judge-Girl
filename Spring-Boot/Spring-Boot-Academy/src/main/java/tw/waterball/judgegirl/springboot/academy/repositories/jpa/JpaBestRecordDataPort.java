package tw.waterball.judgegirl.springboot.academy.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface JpaBestRecordDataPort extends JpaRepository<BestRecordData, BestRecordData.Id> {
}
