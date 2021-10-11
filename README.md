# Judge Girl
 
Judge Girl is under incubation and is [actively developed (here to see the Scrum kanban)](https://github.com/orgs/Judge-Girl/projects/3). The document may be obsolete. Feel free to contact `johnny850807@gmail.com` or leave
issues, open a thread in the discussions page if you are interested in any apsect.


## Setup & Build

Clone the project (must include the submodule):
`git clone --recursive` (This repository depends on the submodule Judge-Girl/Code-Quality-Inspection so the `--recursive` flag is needed)

1. Rename `application.example.yml`, `application-mysql.example.yml`, `application-mongo.example.yml` and `application-redis.example.yml` under the `Spring-Boot/Spring-Boot-Commons/src/main/resources/config/` folder to `application.yml`, `application-mysql.yml`, `application-mongo.yml` and `application-redis.yml`  respectively, and then replace the PLACEHOLDERs in these files with the values to fit your need.
2. Rename `infra.example.yml` to `infra.yml` and replace the PLACEHOLDERs with the values in the file to fit your need.

## Run the entire Judge-Girl's backend locally

For those who wants to run _all-in-one deployment_ locally, please do the following:

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

**This project is a taste of the practicality of (Topological) Microservice, Clean Architecture and Domain-Driven Design (DDD)**.
Topological Microservice emphasizes that Judge-Girl should be capable of being deployed in a form of **both monoliths or microservices**. (Currently, we only support the microservices option)

Here are some overview introductions of our software design. For more details, please navigate to the `docs/` folder.

Judge Girl follows clean-architecture and DDD. The package structure is based on **four bounded context (Student, Problem,
Submission, Academy)** with **three design forces (Domain Logic, Tech Stack, Client)**.

The following represents our primary package structure that **practices [clean architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)**.  <br>

```
├── API (Force: Client)
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

In every domain package, there are application **use cases** (request + presenters), **domain services** and **repositories**. Every repository encapsulates the persistency of an aggregate root (e.g., Student/Problem/Submission) and every use case
represents an application feature.

#### Tech Stack & System Architecture

![image](../docs/images/Domain%20Class%20Diagram.png)

**Spring Boot** is used to run our Judge Girl application.


Never write logs in the domain layer
===

We would log in technical layer (Controller, technical implementations, JPA, ...), **but never in our domain layer.**
We use **Aspect-Oriented Programming** to inject logs into our domain layer.

### Software Testing

Judge Girl is developed under E2E (API --> Database) test cases covering application usecases and Unit Test covering
domain logic and utils. Where in our tests, RabbitMQ, MongoDB and MySQL are **embedded**.
