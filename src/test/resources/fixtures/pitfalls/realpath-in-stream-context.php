<?php

    /* the main case */
    <error descr="realpath() working differently in stream context (e.g. for phar://...), consider using dirname() instead">realpath</error> (__DIR__ . '/../');

    /*similar cases, when the issues will popup */
    include realpath('/whatever/file.php');
    include_once realpath('/whatever/file.php');
    require realpath('/whatever/file.php');
    require_once realpath('/whatever/file.php');

    /* same, but wrapped with () */
    include (realpath('/whatever/file.php'));


    /* false-positives */
    echo(realpath(__DIR__ . '/'));