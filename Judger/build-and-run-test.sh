#!/bin/bash
cd .. && ./mvnw package -DskipTests && cd Judger && \
  docker build . -t judger -f Dockerfile.cqi \
  && sh ccjudger-test.sh