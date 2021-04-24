#!/bin/bash

#
# Copyright 2020 Johnny850807 (Waterball) 潘冠辰
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#       http://www.apache.org/licenses/LICENSE-2.0
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

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
  -v "$(pwd)"/judgeCases:/judgeCases \
  -v "$(pwd)"/junit-platform-console-standalone-1.7.0.jar:/junit5.jar \
  -e JUDGER_TEST_PROBLEM_HOME=/judgeCases \
   "$judgerImageId" \
     java -jar junit5.jar -cp /judger.jar \
     --scan-class-path --include-classname "$tests_classname_pattern" \
     --details verbose
fi

