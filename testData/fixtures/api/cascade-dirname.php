<?php

    <warning descr="[EA] 'dirname(__DIR__, 3)' can be used instead (reduces number of calls).">dirname(dirname(dirname(__DIR__)))</warning>;
    <warning descr="[EA] 'dirname(trim(__DIR__), 3)' can be used instead (reduces number of calls).">dirname(dirname(dirname(trim(__DIR__))))</warning>;
    <warning descr="[EA] 'dirname(__DIR__, 1 + $level)' can be used instead (reduces number of calls).">dirname(dirname(__DIR__, $level))</warning>;
    <warning descr="[EA] 'dirname(__DIR__, 3)' can be used instead (reduces number of calls).">dirname(dirname(__DIR__, 1), 2)</warning>;

    /* false-positives */
    dirname(realpath(__DIR__), 2);
    dirname();
    dirname(dirname());
    dirname(__DIR__);
    dirname(__DIR__, 1);
    dirname(trim(__DIR__));
    dirname(trim(__DIR__), 1);
    dirname(__DIR__, 3);