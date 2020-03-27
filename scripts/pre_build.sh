#!/bin/bash

if [ -z "$1" ]
  then
    echo "Usage: pre_build.sh secrets_folder_path android_sdk_path"
    exit 1
fi

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "Script Dir: $SCRIPT_DIR"
SECRETS_PATH=$1

echo "Copying ${SECRETS_PATH} to ${SCRIPT_DIR}/../secrets"

cp -r ${SECRETS_PATH}/. ${SCRIPT_DIR}/../secrets

touch ${SCRIPT_DIR}/autoVersion.properties