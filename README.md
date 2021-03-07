# Judge Girl

## Setup & Build

Use `ssh` key (ssh is a MUST, or it will fail) to run the following command:
`git clone --recursive` (This repository depends on the submodule Judge-Girl/Code-Quality-Inspection)

<<<<<<< Updated upstream
1. Copy `Spring-Boot/Spring-Boot-Commons/src/main/resources/config/application.example.yml` to the name `application.yml`
and replace the PLACEHOLDERs with the values according to your environment.
2. Copy `Spring-Boot/Spring-Boot-Student/src/main/resources/applicaton.example.yml` to the name `application.yml`
=======
1. Copy `Spring-Boot/Spring-Boot-Commons/src/main/resources/config/application.example.yaml` file its name changed to  `application.yaml`
and replace the PLACEHOLDERs with the values according to your environment.
2. Copy `Spring-Boot/Spring-Boot-Student/src/main/resources/applicaton.example.yaml` file with its name changed to  `application.yaml`
>>>>>>> Stashed changes
and replace the PLACEHOLDERs with the values according to your environment.
3. (Optional: only required to run the integration test) Copy `Spring-Boot/Spring-Boot-Student/src/test/resources/test.example.properties` file with its name changed to `test.properties`
and replace the PLACEHOLDERs with the values according to your environment.
4. Copy `all-in-one.example.yaml` file its name changed to `all-in-one.yaml` and replace the PLACEHOLDERs with the values according to your environment.

4. Run the script to build up the project
`sh run.sh`

<<<<<<< Updated upstream
5. Run docker-compose with `all-in-one.yml` file
    1. Copy `all-in-one.example.yml` to the name `all-in-one.yml`
       and replace the PLACEHOLDERs with the values according to your environment.
    2. run `docker-compose -f all-in-one.yml up`
=======
5. Run docker-compose with `all-in-one.yaml` file
>>>>>>> Stashed changes

