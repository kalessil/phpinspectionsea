<?php

    /* the main case */
    <error descr="realpath() working differently in stream context (e.g. for phar://...), consider using dirname() instead">realpath</error> (__DIR__ . '/../');

    /*similar cases, when the issues will popup */
    include <error descr="realpath() working differently in stream context (e.g. for phar://...), consider using dirname() instead">realpath</error> ('/whatever/file.php');
    include_once <error descr="realpath() working differently in stream context (e.g. for phar://...), consider using dirname() instead">realpath</error> ('/whatever/file.php');
    require <error descr="realpath() working differently in stream context (e.g. for phar://...), consider using dirname() instead">realpath</error> ('/whatever/file.php');
    require_once <error descr="realpath() working differently in stream context (e.g. for phar://...), consider using dirname() instead">realpath</error> ('/whatever/file.php');

    /* same, but wrapped with () */
    include (<error descr="realpath() working differently in stream context (e.g. for phar://...), consider using dirname() instead">realpath</error> ('/whatever/file.php'));


    /* false-positives */
    echo(realpath(__DIR__ . '/'));