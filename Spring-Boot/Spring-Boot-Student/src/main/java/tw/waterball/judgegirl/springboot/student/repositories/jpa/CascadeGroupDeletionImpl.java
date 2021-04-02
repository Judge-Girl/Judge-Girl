package tw.waterball.judgegirl.springboot.student.repositories.jpa;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author - wally55077@gmail.com
 */
@Repository
public class CascadeGroupDeletionImpl implements CascadeGroupDeletion {

    @PersistenceContext
    private EntityManager em;

    @Modifying
    @Transactional
    @Override
    public void deleteById(int groupId) {
        em.createNativeQuery("DELETE FROM groups_students WHERE group_id = ?1")
                .setParameter(1, groupId).executeUpdate();

        int deletedCount = em.createNativeQuery("DELETE FROM \"groups\" WHERE id = ?1")
                .setParameter(1, groupId).executeUpdate();

        if (deletedCount == 0) {
            throw new NotFoundException();
        }
    }
}
