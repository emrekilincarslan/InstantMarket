#!/bin/bash

check_exit_status() {
	EXIT_STATUS=$?
	if [ ! $EXIT_STATUS -eq 0 ];
		then
			exit EXIT_STATUS
	fi
}

# Jenkins
# secrets path - /Users/jenkins/google-play/financex/secrets
# android sdk path - /Users/jenkins/Library/Android/sdk

if [ -z "$3" ]
  then
    echo "Usage: upload_to_google_play.sh secrets_folder_path android_sdk_path fastlane_path"
    exit 1
fi

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "Script Dir: $SCRIPT_DIR"

SECRETS_PATH=$1
echo "SECRETS_PATH: $SECRETS_PATH"

ANDROID_SDK_PATH=$2
export ANDROID_HOME=$ANDROID_SDK_PATH
echo "ANDROID_SDK_PATH: $ANDROID_SDK_PATH"

FASTLANE_PATH=$3
echo "FASTLANE_PATH: $FASTLANE_PATH"

FOURTH_ARGUMENT=$4
FASTLANE_COMMAND=${FOURTH_ARGUMENT:-upload_financex_alpha} # use fourth argument as fastlane command or default to beta
echo "FASTLANE_COMMAND: $FASTLANE_COMMAND"

echo ""
echo ""

echo $(pwd)
$SCRIPT_DIR/pre_build.sh $SECRETS_PATH

check_exit_status

echo "Run auto-version.sh"
cd $SCRIPT_DIR
./auto-version.sh

check_exit_status

cd $SCRIPT_DIR/..
echo "Clean"
./gradlew clean

check_exit_status

echo "Run fastlane $FASTLANE_COMMAND"
$FASTLANE_PATH $FASTLANE_COMMAND

check_exit_status

exit $EXIT_STATUS