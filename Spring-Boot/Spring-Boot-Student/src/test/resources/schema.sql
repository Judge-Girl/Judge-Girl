/* Schema used for testing
   Before commit any modifications to this file,
   first check if the modifications should also be done in etc/init-databases.sql
 */


create table students
(
    id       int auto_increment primary key,
    admin    bit          not null,
    email    varchar(255) null,
    name     varchar(255) null,
    password varchar(255) null
);

create unique index students_email_index on students (email);

create table student_groups
(
    id   int auto_increment primary key,
    name varchar(255) null unique
);

create unique index groups_name_index on student_groups (name);


create table membership
(
    group_id   int not null,
    student_id int not null,
    primary key (group_id, student_id),
    foreign key (group_id) references student_groups (id),
    foreign key (student_id) references students (id)
);










