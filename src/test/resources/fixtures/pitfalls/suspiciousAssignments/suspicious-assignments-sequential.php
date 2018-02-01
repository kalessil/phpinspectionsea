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

    /* false-positive: self-assignment after assignment */
    $y9 = 'whatever';
    if ($x) {
        $y9 .= 'xxx';
    }
    $y10  = 'whatever';
    $y10 .= 'xxx';

    /* false-positives: array append operations */
    $y10          = [];
    $y10[]['pos'] = 0;
    $y10[]['pos'] = 1;

    /* false-positives: self injection in strings */
    $y11 = '';
    $y11 = "$y11";

    /* false-positive: conditional write-consume cycle */
    if ($x) {
        $y12 = null;
        if ($y12 === null) { return; }
    }
    $y12 = null;
    if ($x) {
        $y13 = null;
        $y13 = (string)$y13;
    }
    $y13 = null;

    return [$y1, $y2, $y3, $y4, $y5, $y6, $y7, $y8, $y9, $y10, $y10, $y11, $y12, $y13];
}

function increments_decrements_cases_holder ()
{
    $index = 0;

    $x[$index++] = '';
    $x[$index++] = '';

    $y[++$index] = '';
    $y[++$index] = '';

    $z[$index--] = '';
    $z[$index--] = '';

    $t[--$index] = '';
    $t[--$index] = '';

    return [$x, $y, $z, $t];
}