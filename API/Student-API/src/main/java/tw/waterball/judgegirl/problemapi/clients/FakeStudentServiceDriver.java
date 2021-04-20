package tw.waterball.judgegirl.problemapi.clients;

import tw.waterball.judgegirl.entities.Student;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.filterToList;

public class FakeStudentServiceDriver implements StudentServiceDriver {

    private final Map<String, Student> students = new HashMap<>();

    @Override
    public List<Student> getStudentsByIds(List<Integer> ids) {
        return filterToList(students.values(), student -> ids.contains(student.getId()));
    }

    @Override
    public List<Student> getStudentsByEmails(List<String> emails) {
        return filterToList(students.values(), student -> emails.contains(student.getEmail()));
    }

    public void addStudent(Student student) {
        students.put(student.getEmail(), student);
    }

    public void clear() {
        students.clear();
    }
}
