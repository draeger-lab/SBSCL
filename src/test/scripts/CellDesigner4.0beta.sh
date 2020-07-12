#!/bin/sh
###########################################################################
#
# CellDesigner4.0beta.sh - CellDesigner version 4.0beta for Linux
#
# Copyright (c) 2006 The Systems Biology Institute. All rights reserved.
#
###########################################################################

APP_NAME="CellDesigner4.0beta"

CLASS_PATH="`ls exec/*.jar | tr "\n" ":"`"
CLASS_PATH=${CLASS_PATH}:"`ls lib/*.jar | tr "\n" ":"`"


JAR_ARGS=-sbwmodule

CD_PREFIX="/home/e/erhardf/CellDesigner4.0beta"

MAIN_CLASS=jp.sbi.celldesigner.Application

JAVA="${CD_PREFIX}/jre/bin/java"
VM_ARGS="-Xms32M -Xmx512M -Djava.library.path=."

if [ ! -d "${CD_PREFIX}" ]; then
   echo "Error!! ${CD_PREFIX}: No such directory."
   exit 1
fi

cd "${CD_PREFIX}"

"${JAVA}" ${VM_ARGS} -cp ${CLASS_PATH} ${MAIN_CLASS} ${JAR_ARGS}  

exit 0
