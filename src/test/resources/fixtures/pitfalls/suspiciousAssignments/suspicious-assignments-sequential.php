<?php

function immediateOverrides()
{
    /* case 1: possible override, if itself */
    if ($x) { $y1 = ''; }
    <error descr="$y1 is immediately overridden, perhaps it was intended to use 'else' here.">$y1 = ''</error>;

    /* false-positive: if ends with an exit point */
    if ($x) {
        $y2 = '';
        return $y2;
    }
    $y2 = '';

    /* false-positive: if has alternative branches */
    if ($x) { $y3 = ''; }
    else    { $y3 = ''; }
    $y3 = '';

    /* false-positive: 2nd write is optional */
    $y4 = '';
    if ($x) { $y4 = ''; }


    /* case 2: guaranteed override */
    $y5 = '';
    <error descr="$y5 is immediately overridden, please check this code fragment.">$y5</error> = 'y5';

    /* false-positive: depends on itself */
    $y6 = '';
    $y6 = trim($y6);

    /* false-positive: accumulation */
    $y7[] = '';
    $y7[] = '';

    /* false-positive: assignments by reference */
    $array = [0, 0];
    $y8 = &$array[0];
    $y8 = $array[1];

    return [$y1, $y2, $y3, $y4, $y5, $y6, $y7, $y8];
}