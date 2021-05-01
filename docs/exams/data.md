# Exam service's data design

## Examinee

`examinees(exam_id, session_id, student_id, group_id)`


Every examinee is under a session of an exam, and it associates to an union (Student | Group).
This means that an examinee can be sourced from a group of students, or an individual student.

For example: 

**Given** an exam (id=1) with two sessions `[S1, S2]`, students `[A, B, C, D, E]` and two groups `[G1, G2]`, where `G1` contains the members `A, B` and `G2` contains the members `C, D`.

We can have the following examinee rows:
```sql
(1, S2, A, G2)
(1, S1, B, G1)
(1, S1, C, G2)
(1, S1, D, G2)
(1, S1, E, null)
```

We can see that `G1` and `G2` and `E` are added as examinees under `S1`,
and then `A` is transferred to another session `S2`.




