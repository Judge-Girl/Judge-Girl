package tw.waterball.judgegirl.springboot.academy.repositories.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import tw.waterball.judgegirl.academy.domain.repositories.ExamFilter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Repository
public class FilterExamsPortImpl implements FilterExamsPort {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ExamData> findStudentExams(int studentId, ExamFilter.Status status, Date now, Pageable pageable) {
        String jql = "select e from ExamData e inner join e.examinees p";
        List<String> conditionStatements = getConditionStatements(status);
        conditionStatements.add("p.id.studentId = :studentId");
        jql = composeConditions(jql, conditionStatements);
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
        List<String> conditionStatements = getConditionStatements(status);
        jql = composeConditions(jql, conditionStatements);
        Query query = createQuery(jql, pageable);
        if (status != ExamFilter.Status.all) {
            query.setParameter("now", now);
        }
        return getExamDataListFromQuery(query);
    }

    private Query createQuery(String jql, Pageable pageable) {
        return em.createQuery(jql)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());
    }

    private String composeConditions(String jql, List<String> conditionStatements) {
        if (!conditionStatements.isEmpty()) {
            jql += format(" where %s", String.join(" and ", conditionStatements));
        }
        return jql;
    }

    private List<String> getConditionStatements(ExamFilter.Status status) {
        switch (status) {
            case all:
                return new ArrayList<>();
            case past:
                return new ArrayList<>(List.of(":now >= e.endTime"));
            case current:
                return new ArrayList<>(List.of("e.startTime < :now", ":now < e.endTime"));
            case upcoming:
                return new ArrayList<>(List.of(":now < e.startTime"));
            default:
                throw new IllegalStateException("Impossible");
        }
    }

    private List<ExamData> getExamDataListFromQuery(Query query) {
        List<?> result = query.getResultList();
        List<ExamData> examDataList = new ArrayList<>(result.size());
        result.forEach(obj -> examDataList.add((ExamData) obj));
        return examDataList;
    }
}
