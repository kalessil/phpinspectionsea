<?php

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

function AIFExitPoints(array $x) {
    if (0 === count($x)) {
        foreach ($x as &$y) {}
        throw new RuntimeException();
    } else {
        foreach ($x as &$y) {}
        return $x;
    }
}