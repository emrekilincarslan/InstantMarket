#!/bin/sh

#  auto-version.sh
#  DTNHD
#
#  Created by Mark Norgren on 9/16/14.
#  Copyright (c) 2014 Fondova DTN. All rights reserved.


#!/usr/bin/env
# Auto Increment Version Script


###########################################
###                SETUP                ###
###########################################

### GIT IGNORE ###
# Add these lines to .gitignore file
# AutoVersion.swift
# AutoVersion.h

### XCODE PROJECT SETTINGS ###
# Set the following build settings in your project file
# INFOPLIST_PREFIX_HEADER = AppVersion.h;
# INFOPLIST_PREPROCESS = YES;

#
# Set the following general settings
# Version = APP_VERSION
# Build = BUNDLE_VERSION

#
# Add an Aggregate Target to the project
# Add a Run Script Build Phase
# '$SRCROOT/auto-version.sh'

#
# Add a Target Dependency on the Aggregate Target to you project Target

#
# Tag your git repo with the version number (use comment message)
# 'git tag -a 1.0.0 -m "1.0.0"'

###########################################
###              END SETUP              ###
###########################################
TAG_PREFIX=$1

git="`which git`"
echo "GIT: $git"
versionNum=`$git tag --merged | grep "^$TAG_PREFIX[0-9]*\.[0-9]*\.[0-9]*$" | tail -1 | sed -e "s/^$TAG_PREFIX//"`
if [[ $($git status --porcelain) ]]; then
versionNum="${versionNum}-dirty"
fi
repoCommitCount=`git log --pretty=oneline | wc -l | sed 's/\ //g'`
branchName=`$git rev-parse --abbrev-ref HEAD`
gitSha=`$git rev-parse --short HEAD`
gitFullSha=`$git rev-parse HEAD`
buildDate=$(date)
builderName=`$git config user.name`
builderEmail=`$git config user.email`

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

gradlePropertiesFilename=$SCRIPT_DIR/autoVersion.properties
# Create gradle properties file
echo "//  autoVersion.properties" > $gradlePropertiesFilename
echo "//"     >> $gradlePropertiesFilename
echo "//  **** GENERATED CODE ***"     >> $gradlePropertiesFilename
echo "//"     >> $gradlePropertiesFilename
echo "//  Copyright (c) 2017 FondovaDTN. All rights reserved." >> $gradlePropertiesFilename
echo >> $gradlePropertiesFilename
echo "  tagPrefix=$TAG_PREFIX"     >> $gradlePropertiesFilename
echo "  gitVersionTagNumber=$versionNum"     >> $gradlePropertiesFilename
echo "  gitCommitCount=$repoCommitCount"     >> $gradlePropertiesFilename
echo "  gitBranchName=$branchName"            >> $gradlePropertiesFilename
echo "  gitSha=$gitSha"                         >> $gradlePropertiesFilename
echo "  gitFullSha=$gitFullSha"                >> $gradlePropertiesFilename
echo "  buildDate=$buildDate"                  >> $gradlePropertiesFilename
echo "  builderName=$builderName"             >> $gradlePropertiesFilename
echo "  builderEmail=$builderEmail"           >> $gradlePropertiesFilename


echo "script dir: $SCRIPT_DIR"
echo "Finished running auto-version.sh"
cat $gradlePropertiesFilename
