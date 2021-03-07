# Judge-K8S

## DEMO
![DEMO](https://i.imgur.com/MZeadP5.png)

## Build

1. Copy `Spring-Boot/Spring-Boot-Commons/src/main/resources/config/application.example.yml` to the name `application.yml`
and replace the PLACEHOLDERs with the values according to your environment.
2. Copy `Spring-Boot/Spring-Boot-Student/src/main/resources/applicaton.example.yml` to the name `application.yml`
and replace the PLACEHOLDERs with the values according to your environment.
3. Copy `Spring-Boot/Spring-Boot-Student/src/test/resources/test.example.properties` to the name `test.properties`
and replace the PLACEHOLDERs with the values according to your environment.

4. Run the script to build up the project
```shell script
./mvnw install -DskipTests
```

5. Run docker-compose with `all-in-one.yml` file
    1. Copy `all-in-one.example.yml` to the name `all-in-one.yml`
       and replace the PLACEHOLDERs with the values according to your environment.
    2. run `docker-compose -f all-in-one.yml up`

