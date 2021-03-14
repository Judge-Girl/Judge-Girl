# Judge Girl

## Setup & Build

Use `ssh` key (ssh is a MUST, or it will fail) to run the following command:
`git clone --recursive` (This repository depends on the submodule Judge-Girl/Code-Quality-Inspection)

1. Copy `Spring-Boot/Spring-Boot-Commons/src/main/resources/config/application.example.yml` to the name `application.yml`
and replace the PLACEHOLDERs with the values according to your environment.
2. Copy `Spring-Boot/Spring-Boot-Student/src/main/resources/applicaton.example.yml` to the name `application.yml`
and replace the PLACEHOLDERs with the values according to your environment.
3. Copy `all-in-one.example.yml` file its name changed to `all-in-one.yml` and replace the PLACEHOLDERs with the values according to your environment.
4. Run the script to build up the project
`sh run.sh`

5. Run docker-compose with `all-in-one.yml` file
    1. Copy `all-in-one.example.yml` to the name `all-in-one.yml`
       and replace the PLACEHOLDERs with the values according to your environment.
    2. run `docker-compose -f all-in-one.yml up`

