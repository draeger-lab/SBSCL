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
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DgroupId=scpsolver -DartifactId=SCPSolver -Dversion=1.0v2 -Dfile=nmi/scpsolver/1.0/SCPSolver-1.0v2.jar -DlocalRepositoryPath=${LIB_DIR}/maven -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DgroupId=scpsolver -DartifactId=GLPKSolverPack -Dversion=4.35v2 -Dfile=nmi/scpsolver/1.0/GLPKSolverPack-4.35v2.jar -DlocalRepositoryPath=${LIB_DIR}/maven -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DgroupId=scpsolver -DartifactId=LPSOLVESolverPack -Dversion=5.5.2.5 -Dfile=nmi/scpsolver/1.0/LPSOLVESolverPack-5.5.2.5.jar -DlocalRepositoryPath=${LIB_DIR}/maven -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DgroupId=kisao -DartifactId=libkisao -Dversion=1.0.3.1-rc -Dfile=kisao/LibKiSAO/1.0.3.1/libkisao-1.0.3.1-rc.jar -DlocalRepositoryPath=${LIB_DIR}/maven -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
