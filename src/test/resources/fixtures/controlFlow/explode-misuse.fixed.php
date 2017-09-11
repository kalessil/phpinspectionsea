<?php

function explodeMisuse($arg) {
    /* case: misuse */
    echo substr_count($arg, '') + 1;

    /* case: misuse, with variants lookup */
    $a = explode('', $arg);
    echo count ($a);

    /* false-positive: the variable in not one-time use */
    $c = explode('', $arg);
    echo count($c);
    return $c;
}