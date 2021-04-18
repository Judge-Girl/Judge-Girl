package tw.waterball.judgegirl.problemapi.clients;

import tw.waterball.judgegirl.entities.Student;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FakeStudentServiceDriver implements StudentServiceDriver {

    private final Map<String, Student> students = new HashMap<>();

    @Override
    public List<Student> getStudentsByEmails(List<String> emails) {
        return students.values().stream().filter(student -> emails.contains(student.getEmail())).collect(Collectors.toList());
    }

    public void addStudent(Student student) {
        students.put(student.getEmail(), student);
    }

    public void clear() {
        students.clear();
    }
}
