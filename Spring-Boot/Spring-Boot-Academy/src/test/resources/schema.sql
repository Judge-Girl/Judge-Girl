/*  Schema used for testing
   In case you want to modify the schema below,
    you should also modify "etc/init-databases.sql" correspondingly
 */

create table exams
(
    id          int auto_increment primary key,
    description varchar(255) null,
    name        varchar(255) null,
    start_time  datetime     null,
    end_time    datetime     null,
    white_list  varchar(255) null default ''
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
    foreign key (exam_id) references exams (id) on delete cascade,
    foreign key (exam_id, problem_id) references questions (exam_id, problem_id) on delete cascade
);

create table best_records
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
    foreign key (exam_id, problem_id) references questions (exam_id, problem_id) on delete cascade
);

create table homework
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
