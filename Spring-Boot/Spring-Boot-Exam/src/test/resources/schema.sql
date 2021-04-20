/* Schema used for testing
   Before commit any modifications to this file,
   first check if the modifications should also be done in etc/init-databases.sql
 */

create table exams
(
    id          int auto_increment primary key,
    description varchar(255) null,
    name        varchar(255) null,
    start_time  datetime     null,
    end_time    datetime     null
);

create table questions
(
    exam_id        int not null,
    problem_id     int not null,
    question_order int not null,
    quota          int not null,
    score          int not null,
    primary key (exam_id, problem_id),
    foreign key (exam_id) references exams (id) on delete cascade
);

create table examinees
(
    exam_id    int not null,
    student_id int not null,
    score      int null default 0,
    absent     bit null default true,
    primary key (exam_id, student_id),
    foreign key (exam_id) references exams (id) on delete cascade
);

create table answers
(
    number        int          not null,
    exam_id       int          not null,
    problem_id    int          not null,
    student_id    int          not null,
    submission_id varchar(255) null,
    answer_time   datetime     null,
    primary key (number, exam_id, problem_id, student_id),
    foreign key (exam_id) references exams (id) on delete cascade
);

create table best_records
(
    exam_id              int                                                                    not null,
    problem_id           int                                                                    not null,
    student_id           int                                                                    not null,
    maximum_memory_usage bigint                                                                 not null,
    maximum_runtime      bigint                                                                 not null,
    score                int                                                                    not null,
    status               enum ('AC', 'TLE', 'MLE', 'WA', 'CE', 'OLE', 'RE', 'PE', 'SYSTEM_ERR') null,
    submission_time      datetime                                                               null,
    primary key (exam_id, problem_id, student_id),
    foreign key (exam_id) references exams (id) on delete cascade
);

create table homework
(
    id          int auto_increment primary key,
    name        varchar(255) null,
    problem_ids varchar(255) null
);





