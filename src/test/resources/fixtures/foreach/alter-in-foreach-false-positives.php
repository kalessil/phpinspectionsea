<?php

/* false-positive: global context */
/* @var null|string|string[] $files */
foreach ($files as $file) {
    echo $file;
}

/* false-positive: overriding variable before foreach */
/** @param null|string|string[] $values */
function AIFMixedParameters($values) {
    $values = is_array($values) ? $values : explode('...', $values);
    foreach ($values as $string) {}
}

function AIFCommentBeforeUnset(array $x){
    foreach ($x as & $y) {
        echo $y;
    }
    /* comment here */
    unset($y);
}

function AIFDocBlockBeforeUnset(array $x){
    foreach ($x as & $y) {
        echo $y;
    }
    /** dock-block here */
    /** multiple dock-blocks here */
    unset($y);
}

function AIFScope(array $x){
    if (is_array($x)) {
        /* no next statement in the scope context */
        foreach ($x as & $y) {
            ++$y;
        }
    } else {
        ++$x;
    }

    return $x;
}