<?php

    /* the main case */
    dirname(__DIR__) . '/';
    dirname(dirname(dirname(__DIR__))) . '/';
    realpath(__DIR__ . '/..' . '/..');

    /* similar cases, when the issues will popup */
    include '/whatever/file.php';
    include_once '/whatever/file.php';
    require '/whatever/file.php';
    require_once '/whatever/file.php';
    require_once realpath($x);

    /* the same, but wrapped with () */
    include ('/whatever/file.php');


    /* false-positives */
    echo(realpath(__DIR__ . '/'));