DROP DATABASE IF EXISTS judgegirl;
CREATE DATABASE IF NOT EXISTS judgegirl;
USE judgegirl;

create table answers
(
    exam_id       int          not null,
    number        int          not null,
    problem_id    int          not null,
    student_id    int          not null,
    answer_time   datetime     null,
    submission_id varchar(255) null,
    primary key (exam_id, number, problem_id, student_id)
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
    primary key (exam_id, problem_id, student_id)
);

create table exam_participations
(
    exam_id    int not null,
    student_id int not null,
    score      int null default 0,
    absent     bit null default true,
    primary key (exam_id, student_id)
);

create table exams
(
    id          int auto_increment primary key,
    description varchar(255) null,
    name        varchar(255) null,
    start_time  datetime     null,
    end_time    datetime     null
);

create table `groups`
(
    id   int auto_increment primary key,
    name varchar(255) null unique
);

create unique index groups_name_index on `groups` (name);


create table membership
(
    group_id   int not null,
    student_id int not null,
    primary key (group_id, student_id)
);

create table homework
(
    id          int auto_increment primary key,
    name        varchar(255) null,
    problem_ids varchar(255) null
);

create table questions
(
    exam_id        int not null,
    problem_id     int not null,
    question_order int not null,
    quota          int not null,
    score          int not null,
    primary key (exam_id, problem_id)
);


create table students
(
    id       int auto_increment primary key,
    admin    bit          not null,
    email    varchar(255) null,
    name     varchar(255) null,
    password varchar(255) null
);

create unique index students_email_index on students (email);









