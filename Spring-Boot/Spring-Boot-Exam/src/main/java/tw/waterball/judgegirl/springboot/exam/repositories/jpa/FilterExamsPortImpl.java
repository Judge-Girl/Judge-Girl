package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import tw.waterball.judgegirl.examservice.repositories.ExamFilter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Repository
public class FilterExamsPortImpl implements FilterExamsPort {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ExamData> findStudentExams(int studentId, ExamFilter.Status status, Date now, Pageable pageable) {
        String jql = "select e from ExamData e inner join e.examParticipations p ";
        jql = patchJqlToFilterStatus(jql, status);
        jql += " and p.id.studentId = :studentId";
        Query query = createQuery(jql, pageable)
                .setParameter("studentId", studentId);
        if (status != ExamFilter.Status.all) {
            query.setParameter("now", now);
        }
        return getExamDataListFromQuery(query);
    }

    @Override
    public List<ExamData> findExams(ExamFilter.Status status, Date now, Pageable pageable) {
        String jql = "select e from ExamData e";
        jql = patchJqlToFilterStatus(jql, status);
        Query query = createQuery(jql, pageable);
        if (status != ExamFilter.Status.all) {
            query.setParameter("now", now);
        }
        return getExamDataListFromQuery(query);
    }

    private List<ExamData> getExamDataListFromQuery(Query query) {
        List<?> result = query.getResultList();
        List<ExamData> examDataList = new ArrayList<>(result.size());
        result.forEach(obj -> examDataList.add((ExamData) obj));
        return examDataList;
    }

    private Query createQuery(String jql, Pageable pageable) {
        return em.createQuery(jql)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());
    }


    private String patchJqlToFilterStatus(String jql, ExamFilter.Status status) {
        switch (status) {
            case all:
                return jql;
            case past:
                return jql + " where :now >= e.endTime ";
            case current:
                return jql + " where e.startTime < :now and :now < e.endTime ";
            case upcoming:
                return jql + " where :now < e.startTime ";
            default:
                throw new IllegalStateException("Impossible");
        }
    }
}
