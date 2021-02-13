#!/bin/bash

imageName="judger"
judgerImageId=$(docker images $imageName -q)

if [ "$judgerImageId" == "" ]
then
  printf "Can't find the %s image.\n" "$imageName" >&2
  exit 2
else
  # Run the Junit5 engine to test the judger in the judger container
  # Note: junit-platform-console-standalone-1.7.0.jar is required
  tests_classname_pattern="tw.waterball.judgegirl.judger.tests.*"
  docker run --rm \
  -v "$(pwd)"/junit-platform-console-standalone-1.7.0.jar:/junit5.jar \
#  -v "$(pwd)"/judgerWorkspace:/judger \  # enable for testing
   "$judgerImageId" \
     java -jar junit5.jar -cp /judger.jar \
     --scan-class-path --include-classname "$tests_classname_pattern" \
     --details verbose
fi

