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