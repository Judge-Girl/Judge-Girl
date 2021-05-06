package tw.waterball.judgegirl.springboot.academy.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaHomeworkDAO extends JpaRepository<HomeworkData, Integer> {
}
