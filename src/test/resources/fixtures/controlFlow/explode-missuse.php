<?php

function explodeMissuse($arg) {
    echo <warning descr="Consider using 'substr_count($arg, '') + 1' instead (consumes less cpu and memory resources).">count(explode('', $arg))</warning>;
    $a = explode('', $arg);
    echo <warning descr="Consider using 'substr_count($arg, '') + 1' instead (consumes less cpu and memory resources).">count</warning> ($a);

    echo <warning descr="Consider using 'strstr($arg, '', true)' instead (consumes less cpu and memory resources).">current(explode('', $arg))</warning>;
    $b = explode('', $arg);
    echo <warning descr="Consider using 'strstr($arg, '', true)' instead (consumes less cpu and memory resources).">current</warning> ($b);

    /* false-positive: the variable in not one-time use */
    $c = explode('', $arg);
    echo count($c);
    $d = explode('', $arg);
    echo current($d);
    return [$c, $d];
}