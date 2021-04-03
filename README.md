# Judge Girl

Judge Girl is under incubating, the document may be obsolete.
Feel free to contact `johnny850807@gmail.com` or leave issues if you are interested.

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
