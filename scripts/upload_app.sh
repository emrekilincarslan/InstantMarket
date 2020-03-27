#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "Script Dir: $SCRIPT_DIR"

FASTLANE_PATH=$1

${SCRIPT_DIR}/upload_to_google_play.sh /Users/jenkins/google-play/financex/secrets /Users/jenkins/Library/Android/sdk $FASTLANE_PATH
