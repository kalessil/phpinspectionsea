<?php

function explode_misuse_count($arg) {
    /* case: misuse */
    echo substr_count($arg, '') + 1;

    /* case: misuse, with variants lookup */
    $a = explode('', $arg);
    echo count($a);

    /* false-positive: the variable in not one-time use */
    $c = explode('', $arg);
    echo count($c);
    return $c;
}

function explode_misuse_implode($arg) {
    /* case: misuse */
    echo str_replace('', '...', $arg);

    /* case: misuse, with variants lookup */
    $a = explode('', $arg);
    echo implode('...', $a);

    /* false-positive: the variable in not one-time use */
    $c = explode('', $arg);
    echo implode('', $c);
    return $c;
}

function explode_misuse_in_array($arg) {
    return [
        strpos($arg, 'what'),
    ];
}