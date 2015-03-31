<?php

function foo(&$argByReference, $argByValue) {
    static $theStatic;
    global $theGlobal;

    unset($argByReference[0]);
    unset($theStatic, $theGLobal, $argByReference);
    unset($argByReference); // useless, will only destroy local reference
    unset($argByValue); // useless, will only destroy local copy;

    foreach(array() as $key => $value) {
        unset($value);
    }

}

$varByReference = 1;
$varByValue = 2;
foo($varByReference, $varByValue);