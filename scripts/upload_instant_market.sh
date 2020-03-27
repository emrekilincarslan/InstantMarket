#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "Script Dir: $SCRIPT_DIR"

CURRENT_USER=$(whoami)
echo "CURRENT_USER: $CURRENT_USER"

FIRST_ARGUMENT=$1
echo "FIRST_ARGUMENT: $FIRST_ARGUMENT"
DEFAULT_FASTLANE_PATH="/Users/$CURRENT_USER/.fastlane/bin/fastlane"
FASTLANE_PATH=${FIRST_ARGUMENT:-$DEFAULT_FASTLANE_PATH}
echo "FASTLANE_PATH: $FASTLANE_PATH"

${SCRIPT_DIR}/upload_to_google_play.sh /Users/"$CURRENT_USER"/google-play/stockinstantmarket/secrets /Users/"$CURRENT_USER"/Library/Android/sdk $FASTLANE_PATH upload_instant_market_alpha
