#!/bin/bash

if [ -z "$1" ]
  then
    echo "Usage: set_build_env.sh gradle_properties_file"
    exit 1
fi
GRADLE_PROPERTIES_FILE=$1

ANDROID_KEYSTORE=/Users/mark/working/google-play/financex_android_upload.jks
echo "ANDROID_KEYSTORE='$ANDROID_KEYSTORE'" >> $GRADLE_PROPERTIES_FILE

ANDROID_KEY_STORE_PASSWORD='shining-affront-blat'
echo "ANDROID_KEY_STORE_PASSWORD='$ANDROID_KEY_STORE_PASSWORD'" >> $GRADLE_PROPERTIES_FILE

ANDROID_KEY_ALIAS='financex'
echo "ANDROID_KEY_ALIAS=$ANDROID_KEY_ALIAS" >> $GRADLE_PROPERTIES_FILE

ANDROID_KEY_PASSWORD='shining-affront-blat'
echo "ANDROID_KEY_PASSWORD='$ANDROID_KEY_PASSWORD" >> $GRADLE_PROPERTIES_FILE

BUILD_NUMBER_TAG=$(git tag -l --points-at HEAD | grep build- | sed 's/build-//g')

if [ -z "$BUILD_NUMBER_TAG" ]; then
	echo "*************************************************************************"
    echo "*** Build number tag $BUILD_NUMBER_TAG not set. Required!"
	echo "*************************************************************************"
	exit 1
fi

echo "BUILD_NUMBER_TAG=$BUILD_NUMBER_TAG" >> $GRADLE_PROPERTIES_FILE

