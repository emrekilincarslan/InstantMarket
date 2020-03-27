#!/bin/bash

repoCommitCount=`git log --pretty=oneline | wc -l | sed 's/\ //g'`

git tag "build-$repoCommitCount"

echo "Tagged build with repo commit count $repoCommitCount"

# git push --tags