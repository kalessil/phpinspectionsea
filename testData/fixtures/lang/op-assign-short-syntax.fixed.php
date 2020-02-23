<?php

function cases_holder(int $i, string $string) {
    $i += 2;
    $i -= 2;
    $i *= 2;
    $i /= 2;
    $i .= '2';
    $i &= 2;
    $i |= 2;
    $i ^= 2;
    $i <<= 2;
    $i >>= 2;
    $i %= 2;

    $i = 2 + $i;
    $i = 2 - $i;
    $i = 2 * $i;
    $i = 2 / $i;
    $i = '2' . $i;
    $i = 2 & $i;
    $i = 2 | $i;
    $i = 2 ^ $i;
    $i = 2 << $i;
    $i = 2 >> $i;
    $i = 2 % $i;

    $i .= '=' . $i;
    $i .= '2' . '2';
    $i += 2 + 2;
    $i *= 2 * 2;

    $i = $i . '2' + '2';
    $i = $i / $number / $number;
    $i = $i - $number - $number;
    $i = $i & $number & $number;
    $i = $i | $number | $number;
    $i = $i ^ $number ^ $number;
    $i = $i << $number << $number;
    $i = $i >> $number >> $number;
    $i = $i % $number % $number;

    /* false-positives: string manipulation */
    $string[0] = $string[0] + 1;
}