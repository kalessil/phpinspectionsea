<?php

$x = 1; // <- reported
return $x;

$y = new Exception(); // <- reported
throw $y;

function &returnByReference () {
    $null = null; // <- shall not be reported
    return $null;
}