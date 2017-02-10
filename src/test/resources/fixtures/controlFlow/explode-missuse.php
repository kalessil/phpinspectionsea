<?php

function explodeMissuse($arg) {
    echo <warning descr="Consider refactoring with substr_count() instead (consumes less cpu and memory resources).">count</warning> (explode('', $arg));

    $a = explode('', $arg);
    echo <warning descr="Consider refactoring with substr_count() instead (consumes less cpu and memory resources).">count</warning> ($a);

    /* false-positive: the variable in not one-time use */
    $b = explode('', $arg);
    echo count($b);
    return $b;
}