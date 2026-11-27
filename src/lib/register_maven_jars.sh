#!/bin/bash
#################################################
# Script for registering maven artifacts
#
# If additional jars have to be added register
# these in the same manner like below in the
# repository.
#################################################

# lib directory
LIB_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# register
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DgroupId=kisao -DartifactId=libkisao -Dversion=1.0.3.1-rc -Dfile=kisao/LibKiSAO/1.0.3.1/libkisao-1.0.3.1-rc.jar -DlocalRepositoryPath=${LIB_DIR}/maven -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
