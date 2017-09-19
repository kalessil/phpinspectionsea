<?php

    <warning descr="'dirname(__DIR__, 3)' can be used instead (reduces amount of calls).">dirname(dirname(dirname(__DIR__)))</warning>;
    <warning descr="'dirname(trim(__DIR__), 3)' can be used instead (reduces amount of calls).">dirname(dirname(dirname(trim(__DIR__))))</warning>;
    <warning descr="'dirname(__DIR__, 1 + $level)' can be used instead (reduces amount of calls).">dirname(dirname(__DIR__, $level))</warning>;
    <warning descr="'dirname(__DIR__, 2)' can be used instead (reduces amount of calls).">dirname(dirname(__DIR__, 1), 1)</warning>;

    /* false-positives */
    dirname();
    dirname(dirname());
    dirname(__DIR__);
    dirname(__DIR__, 1);
    dirname(trim(__DIR__));
    dirname(trim(__DIR__), 1);
    dirname(__DIR__, 3);