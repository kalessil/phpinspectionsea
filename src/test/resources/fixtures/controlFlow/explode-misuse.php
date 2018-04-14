<?php

function explode_misuse_count($arg) {
    /* case: misuse */
    echo <warning descr="Consider using 'substr_count($arg, '') + 1' instead (consumes less cpu and memory resources).">count(explode('', $arg))</warning>;

    /* case: misuse, with variants lookup */
    $a = explode('', $arg);
    echo <warning descr="Consider using 'substr_count($arg, '') + 1' instead (consumes less cpu and memory resources).">count($a)</warning>;

    /* false-positive: the variable in not one-time use */
    $c = explode('', $arg);
    echo count($c);
    return $c;
}

function explode_misuse_implode($arg) {
    /* case: misuse */
    echo <warning descr="Consider using 'str_replace('', '...', $arg)' instead (consumes less cpu and memory resources).">implode('...', explode('', $arg))</warning>;

    /* case: misuse, with variants lookup */
    $a = explode('', $arg);
    echo <warning descr="Consider using 'str_replace('', '...', $arg)' instead (consumes less cpu and memory resources).">implode('...', $a)</warning>;

    /* false-positive: the variable in not one-time use */
    $c = explode('', $arg);
    echo implode('', $c);
    return $c;
}