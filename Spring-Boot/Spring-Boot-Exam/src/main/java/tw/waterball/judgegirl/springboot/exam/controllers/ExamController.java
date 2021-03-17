package tw.waterball.judgegirl.springboot.exam.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.springboot.exam.Exam;
import tw.waterball.judgegirl.springboot.exam.ExamParticipation;
import tw.waterball.judgegirl.springboot.exam.repositories.ExamParticipationRepository;
import tw.waterball.judgegirl.springboot.exam.repositories.ExamRepository;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

// TODO: make clean-architecture
@CrossOrigin
@AllArgsConstructor
@RestController
public class ExamController {
    private final ExamRepository examRepository;
    private final ExamParticipationRepository examParticipationRepository;

    @PostMapping("/api/exams")
    public Exam createExam(@Valid @RequestBody Exam exam) {
        exam.validate();
        return examRepository.save(exam);
    }

    @GetMapping("/api/students/{studentId}/exams")
    public List<Exam> getUpcomingExams(@PathVariable Integer studentId, @RequestParam String type) {
        if (type.equals("upcoming")) {
            Date now = new Date();
            List<Exam> examList = findUpcomingExams(studentId);
            return examList.stream()
                    .filter(exam -> exam.getStartTime().after(now))
                    .collect(toList());
        } else {
            throw new IllegalArgumentException("Type: " + type + " not supported.");
        }
    }

    private List<Exam> findUpcomingExams(Integer studentId) {
        List<Integer> examIds = examParticipationRepository.findByStudentId(studentId).stream()
                .map(ExamParticipation::getExamId).collect(toList());
        return examRepository.findByIdIn(examIds);
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<?> errorHandler(Exception err) {
        return ResponseEntity.badRequest().body(err.getMessage());
    }
}
