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









