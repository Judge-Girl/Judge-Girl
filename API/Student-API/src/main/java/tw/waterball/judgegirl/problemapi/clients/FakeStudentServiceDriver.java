package tw.waterball.judgegirl.problemapi.clients;

import tw.waterball.judgegirl.entities.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeStudentServiceDriver implements StudentServiceDriver {

    private final Map<String, Student> students = new HashMap<>();

    @Override
    public List<Student> getStudentsByEmails(List<String> emails) {
        List<Student> studentList = new ArrayList<>();
        for (String email : emails) {
            if (students.containsKey(email)) {
                studentList.add(students.get(email));
            }
        }
        return studentList;
    }

    public void addStudent(Student student) {
        students.put(student.getEmail(), student);
    }

    public void clear() {
        students.clear();
    }
}
