#!/bin/bash
cd .. && ./mvnw package -DskipTests && cd Judger && \
  docker build . -t judger -f Dockerfile.original \
  && sh ccjudger-test.sh