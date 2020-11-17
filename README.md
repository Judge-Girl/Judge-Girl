# Judge-K8S

## DEMO
![DEMO](https://i.imgur.com/MZeadP5.png)

## Build

1. Rename `Spring-Boot/Spring-Boot-Commons/src/main/resources/config/application.example.yaml` to `application.yaml`
and replace the PLACEHOLDERs with the values according to your environment.
2. Rename `Spring-Boot/Spring-Boot-Student/src/main/resources/applicaton.example.yaml` to `application.yaml`
and replace the PLACEHOLDERs with the values according to your environment.
3. Rename `Spring-Boot/Spring-Boot-Student/src/test/resources/test.example.properties` to `test.properties`
and replace the PLACEHOLDERs with the values according to your environment.

4. Run the script to build up the project
```shell script
./mvn install -DskipTests
```

