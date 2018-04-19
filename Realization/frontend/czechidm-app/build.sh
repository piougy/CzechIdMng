#!/bin/bash
# @Autor Ondrej Kopr
# Script for easy build frontend sources

# required versions
GULP_VERSION='3.9.0'
NPM_VERSION='3.5'
NODE_VERSION='4.x.x'

INSTALL_PRODUCT='npm run product-install'
BUILD_PATH='./dist'
STAGE="development"
PROFILE="default"
HELP="help"

echo " ___  _____   __          _      _   _              "
echo "| _ )/ __\ \ / /  ___ ___| |_  _| |_(_)___ _ _  ___"
echo "| _ \ (__ \ V /  (_-</ _ \ | || |  _| / _ \ ' \(_-<"
echo "|___/\___| \_/   /__/\___/_|\_,_|\__|_\___/_||_/__/"

echo "Script for easy building frontend sources"

npmVersion=`npm -v`
nodeVersion=`node -v`
gulpVersion=`gulp -v | tail -1 | awk '{print $4}'`

echo "Npm version: $npmVersion"
echo "Node version: $nodeVersion"
echo "Gulp version: $gulpVersion"

echo

if [[  "$1" = *"${HELP}" ]]; then
  echo "Help command"
  echo "Build with defined stage and profile: './build <stage> <profile>'"
  echo "Build with default stage and profile './build'"
  echo "Default stage: ${STAGE}"
  echo "Default profile: ${PROFILE}"
  exit 0
fi

# gulp version 3.6.0 is required
if [[  "${gulpVersion}" = *"${GULP_VERSION}" ]]; then
  echo "GULP version is correct"
else
  echo "Invalid version of GULP, only supported version is ${GULP_VERSION}"
  exit -1
fi

# node version 4.x.x is required
if test "$( npm -v | awk -F'.' ' ( $1 = 4 || ( $1 == 4 && $2 > 0 ) || ( $1 == 4 && $2 > 0 && $3 > 0 ) ) ' )"
then
  echo "NODE version is correct"
else
  echo "Invalid version of NODE. Version: ${NODE_VERSION} is required."
  exit -1
fi

# npm version 3.6 or higher is required
if test "$( npm -v | awk -F'.' ' ( $1 > 3 || ( $1 == 3 && $2 > 5 ) || ( $1 == 3 && $2 == 5 ) ) ' )"
then
    echo "NPM version is correct"
else
    echo "Invalid version of NPM. Version: ${NPM_VERSION} or higher is required."
    exit -1
fi

echo

echo "Start install product app and all modules in ./czechidm-modules/*"

ls -ld ./czechidm-modules/*/ | awk '{print $9}'

# run npm install product
${INSTALL_PRODUCT}

echo "Start build product app and all modules in ./czechidm-modules/*"

if [[ "x$1" != "x" ]]; then
	STAGE="$1"
	echo "Stage: $STAGE"
fi

if [[ "x$2" != "x" ]]; then
	PROFILE="$2"
	echo "Profile: $PROFILE"
fi

gulp build --stage "$STAGE" --profile "$PROFILE"

echo "BUILD result: $?"

if [[ "$?" = "1" ]]; then
	echo "Build failed"
  exit -1
fi

if [[ "$?" = "0" ]]; then
  cd ${BUILD_PATH}
  finalBuildDirectory=`pwd`

  echo "Success, CzechIdM frontend was build. Build is there ${finalBuildDirectory}"
  exit 0
fi

echo "Unexpect error"
exit -1
