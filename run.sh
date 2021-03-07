#!/bin/sh

docker-compose -f infra.yml up -d
./mvnw clean package -DskipTests
cd Judger/ && docker build . -t judger -f Dockerfile.cqi && cd ..
docker-compose -f services.yml build
docker-compose -f services.yml up