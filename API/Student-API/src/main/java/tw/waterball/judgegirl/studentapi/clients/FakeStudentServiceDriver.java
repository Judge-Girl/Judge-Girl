package tw.waterball.judgegirl.studentapi.clients;

import tw.waterball.judgegirl.primitives.Student;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNullElseGet;
import static java.util.Optional.ofNullable;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.filterToList;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.flatMapToList;

public class FakeStudentServiceDriver implements StudentServiceDriver {

    private final Map<String, Student> students = new HashMap<>();

    @Override
    public List<Student> getStudentsByIds(List<Integer> ids) {
        return filterToList(students.values(), student -> ids.contains(student.getId()));
    }

    @Override
    public List<Student> getStudentsByEmails(List<String> emails) {
        return flatMapToList(emails, email -> ofNullable(students.get(email)).stream());
    }

    public void addStudent(Student student) {
        students.put(student.getEmail(), student);
        student.setId(requireNonNullElseGet(student.getId(), students::size));
    }

    public void clear() {
        students.clear();
    }
}
