<?php

function inline () {
    $a = '';
    return "$a";
}

function &return_by_reference () {
    $null = null;
    return $null;
}

$lambda = function () use (&$x, &$y) {
    $x = $y;
    return $x;
};

function parameter_by_reference (&$x, $y) {
    $x = $y;
    return $x;
}

function self_assignment ($x, $y) {
    $x += $y;
    return $x;
}

function false_positive_from_symfony_profiler() {
    $values = [];
    list($one,) = $values;
    return isset($values[6]) ? $values[6] : null;
}

function false_positive_type_annotation() {
    /* @var object $object */
    $object = call();
    return $object;

    /* @var object|null $object */
    $object = call();
    return $object;

    /** @var object|null $object */
    $object = call();
    return $object;

    /** @var object|null $object */
    $object = call();
    return $object->method();
}