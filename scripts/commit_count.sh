#!/bin/bash

repoCommitCount=`git log --pretty=oneline | wc -l | sed 's/\ //g'`
echo $repoCommitCount