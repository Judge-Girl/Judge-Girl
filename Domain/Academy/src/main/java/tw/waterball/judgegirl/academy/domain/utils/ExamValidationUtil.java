package tw.waterball.judgegirl.academy.domain.utils;

import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.ExamineeOnlyOperationException;
import tw.waterball.judgegirl.primitives.exam.IpAddress;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - wally55077@gmail.com
 */
public class ExamValidationUtil {

    public static void onlyExamineeCanAccessTheExam(boolean isStudent, int studentId, Exam exam) {
        if (isStudent && !exam.hasExaminee(studentId)) {
            throw new ExamineeOnlyOperationException();
        }
    }

    public static void onlyExamineeWithWhitelistIpCanAccessOngoingExam(boolean isStudent, int studentId, IpAddress ipAddress, Exam exam) {
        onlyExamineeCanAccessTheExam(isStudent, studentId, exam);
        if (isStudent && exam.isOngoing() && !exam.hasIpAddress(ipAddress)) {
            throw notFound(Exam.class).id(exam.getId());
        }
    }
}
