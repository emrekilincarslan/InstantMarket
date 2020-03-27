#!/bin/bash

if [ -z "$1" ]
  then
    echo "Usage: run_unit_tests.sh fastlane_executable_path"
    exit 1
fi
FASTLANE_PATH=$1

export FASTLANE_DISABLE_COLORS=1
$FASTLANE_PATH unit_tests
EXIT_STATUS=$?

exit $EXIT_STATUS