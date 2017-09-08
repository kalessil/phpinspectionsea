<?php

function explodeMisuse($arg) {
    /* case: misuse */
    echo <warning descr="Consider using 'substr_count($arg, '') + 1' instead (consumes less cpu and memory resources).">count(explode('', $arg))</warning>;

    /* case: misuse, with variants lookup */
    $a = explode('', $arg);
    echo <warning descr="Consider using 'substr_count($arg, '') + 1' instead (consumes less cpu and memory resources).">count ($a)</warning>;

    /* false-positive: the variable in not one-time use */
    $c = explode('', $arg);
    echo count($c);
    return $c;
}