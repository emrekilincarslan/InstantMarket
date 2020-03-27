#!/bin/bash

echo "Building signed APK..."

./gradlew assembleRelease

APK_PATH='./app/build/outputs/apk/app-release.apk '
APK_MODIFICATION_TIME=$(stat -f "%Sm" -t "%Y-%m-%d %H:%M" $APK_PATH)

echo "Completed. $APK_PATH - $APK_MODIFICATION_TIME"

