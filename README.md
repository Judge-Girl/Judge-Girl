# Judge Girl
 
Judge Girl is under incubation and is [actively developed (here to see the Scrum kanban)](https://github.com/orgs/Judge-Girl/projects/3). The document may be obsolete. Feel free to contact `johnny850807@gmail.com` or leave
issues, open a thread in the discussions page if you are interested in any apsect.

## Setup & Build

Clone the project (must include the submodule):
`git clone --recursive` (This repository depends on the submodule Judge-Girl/Code-Quality-Inspection)

1. Rename `application.example.yml`, `application-mysql.example.yml` and `application-mongo.example.yml` under the `Spring-Boot/Spring-Boot-Commons/src/main/resources/config/` folder to `application.yml`, `application-mysql.yml` and `application-mongo.yml`, respectively.
   and replace the PLACEHOLDERs with the values according to your environment.
2. Rename `infra.example.yml` to `infra.yml` and replace the PLACEHOLDERs with the values according to your environment.

## Run Judge-Girl's backend locally

For those who wants to run all-in-one deployment locally, please do the following:

1. Create a judge-girl network by `docker network create judge-girl`
1. Run the infra component by `docker-compose -f infra.yml`
2. Build and run the application by `sh run.sh`

## Contributing

Contributions are welcome, especially 'software-design' nerds are welcomed to taste the practice of microservice/OOD/clean-architecture/domain-driven-design with us.

Thanks to all the people who already contributed!

<a href="https://github.com/Judge-Girl/Judge-Girl/graphs/contributors">
  <img src="https://contributors-img.web.app/image?repo=Judge-Girl/Judge-Girl" />
</a>

### Software Design (Clean Architecture/DDD)

**This project is a taste of the practicality of the (Topological) Microservice, Clean Architecture and Domain-Driven Design**.
Topological Microservice emphasizes that Judge-Girl is capable of being deployed in a form of **both monoliths or microservices**.

Here are some overview introductions of our software design. For more details, please navigate to the `docs/` folder.

Judge Girl follows clean-architecture and DDD. The package structure is based on **four subdomains (Student, Problem,
Submission, Exam)** and **four design forces (Domain Logic, Tech Stack, View Model, Judger)**.

The following represents our primary package structure that **entirely practices [clean architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)**.  <br>

```
├── API (Force: Client / View Model)
│   ├── API-Commons
│   ├── Judger-API
│   ├── Student-API
│   ├── Problem-API
│   └── Submission-API
├── Domain (Force: Domain)
|   ├── Primitives 
│   ├── Student
│   ├── Problem
│   ├── Submission
│   └── Academy
├── Plugins
├── Dependencies (Maven dependencies)
├── Judger (Force: Online Judge System)
│   ├── CC-Profiler-Sandbox
│   └── Code-Quality-Inspection (Submodule)
└── Spring-Boot (Force: Tech Stack)
    ├── Spring-Boot-Commons
    ├── Spring-Boot-Academy
    ├── Spring-Boot-Problem
    ├── Spring-Boot-Student
    └── Spring-Boot-Submission
```

with 100% respect to the following diagram from Uncle Bob:
![Clean Architecture](https://blog.cleancoder.com/uncle-bob/images/2012-08-13-the-clean-architecture/CleanArchitecture.jpg)


#### Domain

In every domain package, there are application **use cases** (request + presenters), **domain services** and **
repositories**. Every repository encapsulates the persistency of an aggregate root (e.g., Student/Problem/Submission) and every use case
represents a feature.

##### Bounded Contexts

Currently, four bounded-context lives in the Judge-Girl application: **Student / Problem / Submission / Academy**
Each of them can be deployed as a microservice.


#### Tech Stack & System Architecture

![image](https://user-images.githubusercontent.com/23109467/113490957-fb61e400-94ff-11eb-9607-b61615328936.png)

**Spring Boot** is used to run our Judge Girl application.


Never write logs in the domain layer
===

We would log in technical layer (Controller, technical implementations, JPA, ...), **but never in our domain layer.**
We use **Aspect-Oriented Programming** to inject logs into our domain layer.

### Software Testing

Judge Girl is developed under E2E (API --> Database) test cases covering application usecases and Unit Test covering
domain logic and utils. Where in our tests, RabbitMQ, MongoDB and MySQL are **embedded**.
