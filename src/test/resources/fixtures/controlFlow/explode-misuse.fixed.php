<?php

function explodeMisuse($arg) {
    echo substr_count($arg, '') + 1;

    $a = explode('', $arg);
    echo count ($a);

    /* false-positive: the variable in not one-time use */
    $c = explode('', $arg);
    echo count($c);
    return $c;
}