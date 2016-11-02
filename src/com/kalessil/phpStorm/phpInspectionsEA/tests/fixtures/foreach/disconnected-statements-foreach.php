<?php

    // WEAK: This statement seems to be disconnected from parent foreach
    // WEAK: Objects should be created outside of a loop and cloned instead

    // new, dom element create
//    $log = '/tmp/debug';
//
//    /* @var array $files */
//    foreach ($files as &$file1) {
//        error_log('Procesing next file', 3, $log);
//    }

    // loops, ifs, switches, try's needs to be reported on keyword, others - complete