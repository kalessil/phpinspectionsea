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