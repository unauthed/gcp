#!/bin/bash
# consumes an input file generated with:
# mvn dependency:list -DoutputFile=mvn-deps.txt -Dmdep.outputScope=false

set -ex

MAX_RETRIES=5

while read LINE
do
  # fetch the artifact, retrying up to 5 times
  i=0
  until [[ $i -ge $MAX_RETRIES ]]
  do
    mvn --batch-mode org.apache.maven.plugins:maven-dependency-plugin:3.1.1:get -Dartifact="${LINE/:jar}" && break;
    i=$((i + 1))
    sleep 1
  done

  if [[ $i -ge $MAX_RETRIES ]]; then
    echo ">>> Skipping fetch artifact \"$LINE\" after $MAX_RETRIES attempts"
  fi
done < /builder/mvn-deps.txt

