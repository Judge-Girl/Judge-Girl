package tw.waterball.judgegirl.springboot.exam.repositories.jpa.impl;

import org.springframework.stereotype.Repository;
import tw.waterball.judgegirl.springboot.exam.repositories.jpa.GroupData;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Repository
public class GetGroupsOwnByMemberImpl implements GetGroupsOwnByMember {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public Set<GroupData> getGroupsOwnedByMember(int memberId) {
        List<GroupData> result = em.createQuery("SELECT g from membership m inner join m.group g where m.id.memberId = ?1")
                .setParameter(1, memberId).getResultList();
        return new HashSet<>(result);
    }
}
