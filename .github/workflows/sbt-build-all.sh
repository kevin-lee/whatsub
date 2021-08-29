#!/bin/bash -e

set -x

if [ -z "$1" ]
  then
    echo "Missing parameters. Please enter the [Scala version]."
    echo "sbt-build-all.sh 3.0.0"
    exit 1
else
  : ${CURRENT_BRANCH_NAME:?"CURRENT_BRANCH_NAME is missing."}

  scala_version=$1
  echo "============================================"
  echo "Build projects"
  echo "--------------------------------------------"
  echo ""
  export SOURCE_DATE_EPOCH=$(date +%s)
  echo "SOURCE_DATE_EPOCH=$SOURCE_DATE_EPOCH"

  if [[ "$CURRENT_BRANCH_NAME" == "main" || "$CURRENT_BRANCH_NAME" == "release" ]]
  then
    sbt \
      ++${scala_version}! \
      -v \
      clean \
      test \
      packagedArtifacts
#      packagedArtifacts \
#      universal:packageBin

#    ls -ld cli/target/universal/whatsub-cli*
  else
    sbt \
      ++${scala_version}! \
      -v \
      clean \
      test \
      package
  fi


  echo "============================================"
  echo "Building projects: Done"
  echo "============================================"
fi
