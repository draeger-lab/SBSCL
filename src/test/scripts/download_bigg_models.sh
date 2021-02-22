#!/bin/bash
#################################################
# Download bigg models for testing
#
# usage:
#  source ./download_bigg_models.sh
#################################################

set -eo pipefail

# This URL gives the updated BiGG Models from the repository
BIGG_MODELS_BASE_URL="https://www.dropbox.com/sh/ye05djxrpxy37da/AAAPwL27xQ97mHGoLmCxhI-4a/v1.6"
FILE_NAME="models.tgz"

_CWD="$PWD"
TEST_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# download and extract the bigg models for testing
cd $TEST_DIR/../resources/bigg
wget $BIGG_MODELS_BASE_URL/$FILE_NAME
tar -xvzf $FILE_NAME --wildcards '*.xml'
rm $FILE_NAME
cd $TEST_DIR/../resources/bigg/models/

# set environment variable
export BIGG_MODELS_PATH=${TEST_DIR}/../resources/bigg/models

echo $BIGG_MODELS_PATH

# move back
cd $_CWD
