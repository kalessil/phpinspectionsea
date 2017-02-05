<?php

function inline () {
    $a = '';
    return "$a";
}

function &returnByReference () {
    $null = null;
    return $null;
}

$lambda = function () use (&$x, &$y) {
    $x = $y;
    return $x;
};

function parameterByReference (&$x, $y) {
    $x = $y;
    return $x;
}

function selfAssignment ($x, $y) {
    $x += $y;
    return $x;
}

function falsePositiveFromSymfonyProfiler() {
    $values = [];
    list($one,) = $values;
    return isset($values[6]) ? $values[6] : null;
}