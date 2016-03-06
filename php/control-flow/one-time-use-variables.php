<?php

$x = 1; // <- reported
return $x;

$y = new Exception(); // <- reported
throw $y;

function &returnByReference () {
    $null = null; // <- shall not be reported
    return $null;
}

function () use (&$x, &$y) {
    $x = $y;      // <- shall not be reported
    return $x;
}

function x (&$x, $y) {
    $x = $y;      // <- shall not be reported
    return $x;
}