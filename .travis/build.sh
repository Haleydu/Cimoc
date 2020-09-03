#!/bin/bash

set -e
MD5=$(md5sum app/build/outputs/apk/release/Cimoc*.apk)
VERSIONNAME=$(git describe --tags)
printf "\n"
printf "{"
printf "\n"
printf "  versionCode:"
printf $TRAVIS_BUILD_NUMBER
printf "\n"
printf "  versionName:"
printf $VERSIONNAME
printf "\n"
printf "  md5:"
printf $MD5
printf "\n"
printf "}"
printf "\n"

echo "{
  versionCode:\"$TRAVIS_BUILD_NUMBER\"
  versionName:\"$VERSIONNAME\"
  md5:\"$MD5\"
}" > ApkInfo.json

printf "\n"
cat ApkInfo.json