#!/bin/bash

PROJECT_DIR=/home/kalessil/PhpstormProjects/symfony2
PHP_STORM_DIR=/home/kalessil/Apps/PhpStorm-143.1480

# Raw output will be stored under ./Default folder.
# Parameters are:
# 1. Project folder with .idea folder (.idea is needed, or inspection fails)
# 2. File with profiles and profile (we can have our own at any place)
# 3. Additional params: verbosity, which folder in the project to inspect
$PHP_STORM_DIR/bin/inspect.sh \
    $PROJECT_DIR \
    ~/WebIde100/config/inspection/Default.xml Default \
    -v2 -d $PROJECT_DIR/src