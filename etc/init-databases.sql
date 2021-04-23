CREATE DATABASE IF NOT EXISTS judgegirl;
USE judgegirl;


/*  Exam Service's schema definition
   If the schema below is modified,
   then you should also update "Spring-Boot/Spring-Boot-Exam/src/test/resources/schema.sql"
 */

create table if not exists exams
(
    id          int auto_increment primary key,
    description varchar(255) null,
    name        varchar(255) null,
    start_time  datetime     null,
    end_time    datetime     null
);

create table if not exists questions
(
    exam_id        int not null,
    problem_id     int not null,
    question_order int not null,
    quota          int not null,
    score          int not null,
    primary key (exam_id, problem_id),
    foreign key (exam_id) references exams (id) on delete cascade
);

create table if not exists examinees
(
    exam_id    int not null,
    student_id int not null,
    score      int null default 0,
    absent     bit null default true,
    primary key (exam_id, student_id),
    foreign key (exam_id) references exams (id) on delete cascade
);

create table if not exists answers
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

create table if not exists best_records
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

create table if not exists homework
(
    id          int auto_increment primary key,
    name        varchar(255) null,
    problem_ids varchar(255) null
);


/*  Student Service's schema definition
   If the schema below is modified,
   then you should also update "Spring-Boot/Spring-Boot-Student/src/test/resources/schema.sql"
 */

create table if not exists students
(
    id       int auto_increment primary key,
    admin    bit          not null,
    email    varchar(255) null,
    name     varchar(255) null,
    password varchar(255) null
);

create unique index students_email_index on students (email);

create table if not exists student_groups
(
    id   int auto_increment primary key,
    name varchar(255) null unique
);

create unique index groups_name_index on student_groups (name);


create table if not exists membership
(
    group_id   int not null,
    student_id int not null,
    primary key (group_id, student_id),
    FOREIGN KEY (group_id) REFERENCES student_groups (id),
    FOREIGN KEY (student_id) REFERENCES students (id)
);










