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

function x (&$x, $y) {
    $x = $y;
    return $x;
}

function y ($x, $y) {
    $x += $y;
    return $x;
}