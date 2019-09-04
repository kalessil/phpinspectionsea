<?php

/* fixture checked on 5.6 language level */
return [
    <error descr="'PHP_VERSION_ID === 50300' seems to be always false.">PHP_VERSION_ID === 50300</error>,
    PHP_VERSION_ID === 50600,
    PHP_VERSION_ID === 70000,

    <error descr="'PHP_VERSION_ID == 50300' seems to be always false.">PHP_VERSION_ID == 50300</error>,
    PHP_VERSION_ID == 50600,
    PHP_VERSION_ID == 70000,

    <error descr="'PHP_VERSION_ID !== 50300' seems to be always true.">PHP_VERSION_ID !== 50300</error>,
    PHP_VERSION_ID !== 50600,
    PHP_VERSION_ID !== 70000,

    <error descr="'PHP_VERSION_ID != 50300' seems to be always true.">PHP_VERSION_ID != 50300</error>,
    PHP_VERSION_ID != 50600,
    PHP_VERSION_ID != 70000,

    <error descr="'PHP_VERSION_ID > 50300' seems to be always true.">PHP_VERSION_ID > 50300</error>,
    PHP_VERSION_ID > 50600,
    PHP_VERSION_ID > 70000,

    <error descr="'PHP_VERSION_ID >= 50300' seems to be always true.">PHP_VERSION_ID >= 50300</error>,
    <error descr="'PHP_VERSION_ID >= 50600' seems to be always true.">PHP_VERSION_ID >= 50600</error>,
    PHP_VERSION_ID >= 70000,

    <error descr="'PHP_VERSION_ID < 50300' seems to be always false.">PHP_VERSION_ID < 50300</error>,
    <error descr="'PHP_VERSION_ID < 50600' seems to be always false.">PHP_VERSION_ID < 50600</error>,
    PHP_VERSION_ID < 70000,

    <error descr="'PHP_VERSION_ID <= 50300' seems to be always false.">PHP_VERSION_ID <= 50300</error>,
    PHP_VERSION_ID <= 50600,
    PHP_VERSION_ID <= 70000,

    <error descr="'PHP_OS_FAMILY == 'linux'' seems to be always false.">PHP_OS_FAMILY == 'linux'</error>,
    <error descr="'PHP_OS_FAMILY === 'linux'' seems to be always false.">PHP_OS_FAMILY === 'linux'</error>,
    <error descr="'PHP_OS_FAMILY != 'linux'' seems to be always true.">PHP_OS_FAMILY != 'linux'</error>,
    <error descr="'PHP_OS_FAMILY !== 'linux'' seems to be always true.">PHP_OS_FAMILY !== 'linux'</error>,

    PHP_OS_FAMILY === 'Linux',
    PHP_OS_FAMILY !== 'Linux',
];