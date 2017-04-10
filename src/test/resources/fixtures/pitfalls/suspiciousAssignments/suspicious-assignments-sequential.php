<?php

function immediateOverrides()
{
    /* Case 1: possible override */
    if ($x) {
        $y = '';
    }
    <error descr="$y is immediately overridden, perhaps it was intended to use 'else' here.">$y = '';</error>

    /* false-positive: 2nd write is optional */
    $t = '';
    if ($x) {
        $t = '';
    }

    /* Case 2: guaranteed override */
    $z = '';
    <error descr="$z is immediately overridden, please check this.">$z = $y;</error>

    /* false-positive: depends on itself */
    $a = '';
    $a = trim($a);

    return [$y, $z, $t, $a];
}