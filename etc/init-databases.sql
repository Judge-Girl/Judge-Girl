CREATE DATABASE IF NOT EXISTS judgegirl;
USE judgegirl;


/*  Exam Service's schema definition
   If the schema below is modified,
   then you should also update "Spring-Boot/Spring-Boot-Academy/src/test/resources/schema.sql"
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
    foreign key (exam_id) references exams (id) on delete cascade,
    foreign key (exam_id, problem_id) references questions (exam_id, problem_id) on delete cascade,
    foreign key (exam_id, student_id) references examinees (exam_id, student_id) on delete cascade
);

create table if not exists best_records
(
    exam_id              int                                                                    not null,
    problem_id           int                                                                    not null,
    student_id           int                                                                    not null,
    submission_id        varchar(255)                                                           not null,
    maximum_memory_usage bigint                                                                 not null,
    maximum_runtime      bigint                                                                 not null,
    grade                int                                                                    not null,
    max_grade            int                                                                    not null,
    status               enum ('AC', 'TLE', 'MLE', 'WA', 'CE', 'OLE', 'RE', 'PE', 'SYSTEM_ERR') null,
    submission_time      datetime                                                               null,
    primary key (exam_id, problem_id, student_id),
    foreign key (exam_id) references exams (id) on delete cascade,
    foreign key (exam_id, problem_id) references questions (exam_id, problem_id) on delete cascade,
    foreign key (exam_id, student_id) references examinees (exam_id, student_id) on delete cascade
);

create table if not exists homework
(
    id          int auto_increment primary key,
    name        varchar(255) null,
    problem_ids varchar(255) null
);


create table study_groups
(
    id   int auto_increment primary key,
    name varchar(255) null unique
);

create unique index groups_name_index on study_groups (name);


create table membership
(
    group_id  int not null,
    member_id int not null,
    primary key (group_id, member_id),
    foreign key (group_id) references study_groups (id) on delete cascade
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








