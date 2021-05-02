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








