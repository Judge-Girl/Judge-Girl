package tw.waterball.judgegirl.springboot.exam.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.springboot.exam.Exam;
import tw.waterball.judgegirl.springboot.exam.ExamParticipation;
import tw.waterball.judgegirl.springboot.exam.repositories.ExamParticipationRepository;
import tw.waterball.judgegirl.springboot.exam.repositories.ExamRepository;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class ExamController {
    private final ExamRepository examRepository;
    private final ExamParticipationRepository examParticipationRepository;

    public ExamController(ExamRepository examRepository, ExamParticipationRepository examParticipationRepository) {
        this.examRepository = examRepository;
        this.examParticipationRepository = examParticipationRepository;
    }

    @PostMapping("/api/exams")
    public Exam createExam(@Valid @RequestBody Exam exam) {
        exam.validate();
        return examRepository.save(exam);
    }

    @GetMapping("/api/students/{studentId}/exams")
    public List<Exam> getUpcomingExams(@PathVariable Integer studentId, @RequestParam String type) {
        if (type.equals("upcoming")) {
            List<ExamParticipation> examParticipations = examParticipationRepository.findByStudentId(studentId);
            List<Integer> examIds = new ArrayList<>();
            for (ExamParticipation examParticipation : examParticipations) {
                examIds.add(examParticipation.getExamId());
            }
            List<Exam> examList = examRepository.findByIdIn(examIds);
            List<Exam> result = new ArrayList<>();
            for (Exam exam : examList) {
                if (exam.getStartTime().after(new Date())) {
                    result.add(exam);
                }
            }
            return result;
        } else throw new IllegalArgumentException("type " + type + " not supported.");
    }

    @ExceptionHandler({IllegalStateException.class})
    public ResponseEntity<?> errorHandler(Exception err) {
        return ResponseEntity.badRequest().build();
    }
}
