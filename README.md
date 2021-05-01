# Judge Girl

Judge Girl is under incubating, the document may be obsolete. Feel free to contact `johnny850807@gmail.com` or leave
issues if you are interested.

## Setup & Build (Obsolete)

Use `ssh` key (ssh is a MUST, or it will fail) to run the following command:
`git clone --recursive` (This repository depends on the submodule Judge-Girl/Code-Quality-Inspection)

1. Copy `Spring-Boot/Spring-Boot-Commons/src/main/resources/config/application.example.yml` to the
   name `application.yml`
   and replace the PLACEHOLDERs with the values according to your environment.
2. Copy `infra.example.yml` file with its name changed to `infra.yml` and replace the PLACEHOLDERs with the values
   according to your environment.

## Run Judge-Girl's backend

1. `docker-compose -f infra.yml`
2. `sh run.sh`

## Contributing

Thanks to all the people who already contributed!


<a href="https://github.com/Judge-Girl/Judge-Girl/graphs/contributors">
  <img src="https://contributors-img.web.app/image?repo=Judge-Girl/Judge-Girl" />
</a>

### Software Design (Clean Architecture/DDD)

Judge Girl follows clean-architecture and DDD. The package structure is based on **four subdomains (Student, Problem,
Submission, Exam)** and **four design forces (Domain Logic, Tech Stack, View Model, Judger)**.

The following represents the ideas of our package structure.  <br>
(You may find this is not the current representation of the project. However, this is an improvement that will be done
in the next sprint)

```
├── API (Force: Client / View Model)
│   ├── API-Commons
│   ├── Judger-API
│   ├── Student-API
│   ├── Problem-API
│   └── Submission-API
├── Domain (Force: Domain)
|   ├── Primitives 
│   ├── Student-Domain
│   ├── Problem-Domain
│   ├── Submission-Domain
│   └── Exam-Domain
├── Plugins
├── Judge-Girl-Dependencies (Maven dependencies)
├── Judger (Force: Online Judge System)
│   ├── CC-Profiler-Sandbox
│   └── Code-Quality-Inspection (Submodule)
└── Spring-Boot (Force: Tech Stack)
    ├── Spring-Boot-Commons
    ├── Spring-Boot-Exam
    ├── Spring-Boot-Problem
    ├── Spring-Boot-Student
    └── Spring-Boot-Submission
```

#### Domain

In every domain package, there will be application **use cases** (request + presenters), **domain services** and **
repositories**. With every repository manages an aggregate (e.g., Student/Problem/Submission) and every use case
represents a feature.

#### Tech Stack & System Architecture

![image](https://user-images.githubusercontent.com/23109467/113490957-fb61e400-94ff-11eb-9607-b61615328936.png)

**Spring Boot** is used to run our Judge Girl application entirely.



Never write logs in domain layer
===

We would log in technical layer (Controller, technical implementations, JPA, ...), **but never in our domain layer.**
We use **Aspect-Oriented Programming** to inject logs into our domain layer.

### Software Testing

Judge Girl is developed under E2E (API --> Database) test cases covering application usecases and Unit Test covering
domain logic and utils. Where in our test, RabbitMQ, MongoDB and MySQL are **embedded** (TestContainers is not required)
.
