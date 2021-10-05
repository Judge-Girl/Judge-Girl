/*  Schema used for testing
   In case you want to modify the schema below,
    you should also modify "etc/init-databases.sql" correspondingly
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








