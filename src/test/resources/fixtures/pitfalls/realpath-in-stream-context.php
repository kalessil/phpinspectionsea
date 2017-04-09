<?php

    /* the main case */
    <error descr="'dirname(__DIR__) . '/'' should be used instead (due to how realpath handles streams).">realpath (__DIR__ . '/../')</error>;
    <error descr="'dirname(dirname(dirname(__DIR__))) . '/'' should be used instead (due to how realpath handles streams).">realpath (__DIR__ . '/../../../')</error>;
    <error descr="'realpath()' works differently in a stream context (e.g., for phar://...). Consider using 'dirname()' instead.">realpath</error> (__DIR__ . '/..' . '/..');

    /* similar cases, when the issues will popup */
    include <error descr="''/whatever/file.php'' should be used instead (due to how realpath handles streams).">realpath ('/whatever/file.php')</error>;
    include_once <error descr="''/whatever/file.php'' should be used instead (due to how realpath handles streams).">realpath ('/whatever/file.php')</error>;
    require <error descr="''/whatever/file.php'' should be used instead (due to how realpath handles streams).">realpath ('/whatever/file.php')</error>;
    require_once <error descr="''/whatever/file.php'' should be used instead (due to how realpath handles streams).">realpath ('/whatever/file.php')</error>;
    require_once <error descr="'realpath()' works differently in a stream context (e.g., for phar://...). Consider using 'dirname()' instead.">realpath</error> ($x);

    /* the same, but wrapped with () */
    include (<error descr="''/whatever/file.php'' should be used instead (due to how realpath handles streams).">realpath ('/whatever/file.php')</error>);


    /* false-positives */
    echo(realpath(__DIR__ . '/'));