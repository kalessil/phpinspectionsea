<?php

function foo(&$argByReference, $argByValue) {
    static $theStatic;
    global $theGlobal;

    unset($argByReference[0]);
    unset($theStatic);
    unset($theGLobal);

    <weak_warning descr="[EA] Only local copy/reference will be unset. This unset can probably be removed.">unset($argByReference);</weak_warning>
    <weak_warning descr="[EA] Only local copy/reference will be unset. This unset can probably be removed.">unset($argByValue);</weak_warning>
    unset(
        <weak_warning descr="[EA] Only local copy/reference will be unset. This unset can probably be removed.">$argByReference</weak_warning>,
        <weak_warning descr="[EA] Only local copy/reference will be unset. This unset can probably be removed.">$argByValue</weak_warning>
    );

    foreach(array() as $key => $value) {
        unset($value);
    }
}

$varByReference = 1;
foo($varByReference, 2);