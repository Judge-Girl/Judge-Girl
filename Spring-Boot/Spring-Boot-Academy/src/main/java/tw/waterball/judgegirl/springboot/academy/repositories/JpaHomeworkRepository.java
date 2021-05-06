package tw.waterball.judgegirl.springboot.academy.repositories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.academy.domain.repositories.HomeworkRepository;
import tw.waterball.judgegirl.primitives.Homework;
import tw.waterball.judgegirl.springboot.academy.repositories.jpa.HomeworkData;
import tw.waterball.judgegirl.springboot.academy.repositories.jpa.JpaHomeworkDAO;

import java.util.Optional;

import static tw.waterball.judgegirl.springboot.academy.repositories.jpa.HomeworkData.toData;

/**
 * @author - wally55077@gmail.com
 */
@Component
@AllArgsConstructor
public class JpaHomeworkRepository implements HomeworkRepository {

    private final JpaHomeworkDAO jpaHomeworkDAO;

    @Override
    public Homework save(Homework homework) {
        HomeworkData homeworkData = jpaHomeworkDAO.save(toData(homework));
        homework.setId(homeworkData.getId());
        return homework;
    }

    @Override
    public Optional<Homework> findHomeworkById(int homeworkId) {
        return jpaHomeworkDAO.findById(homeworkId)
                .map(HomeworkData::toEntity);
    }

    @Override
    public void deleteAll() {
        jpaHomeworkDAO.deleteAll();
    }
}
