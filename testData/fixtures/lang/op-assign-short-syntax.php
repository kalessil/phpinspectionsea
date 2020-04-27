<?php

function cases_holder(int $i, string $string) {
    <weak_warning descr="[EA] Can be safely refactored as '$i += 2'.">$i = $i + 2</weak_warning>;
    <weak_warning descr="[EA] Can be safely refactored as '$i -= 2'.">$i = $i - 2</weak_warning>;
    <weak_warning descr="[EA] Can be safely refactored as '$i *= 2'.">$i = $i * 2</weak_warning>;
    <weak_warning descr="[EA] Can be safely refactored as '$i /= 2'.">$i = $i / 2</weak_warning>;
    <weak_warning descr="[EA] Can be safely refactored as '$i .= '2''.">$i = $i . '2'</weak_warning>;
    <weak_warning descr="[EA] Can be safely refactored as '$i &= 2'.">$i = $i & 2</weak_warning>;
    <weak_warning descr="[EA] Can be safely refactored as '$i |= 2'.">$i = $i | 2</weak_warning>;
    <weak_warning descr="[EA] Can be safely refactored as '$i ^= 2'.">$i = $i ^ 2</weak_warning>;
    <weak_warning descr="[EA] Can be safely refactored as '$i <<= 2'.">$i = $i << 2</weak_warning>;
    <weak_warning descr="[EA] Can be safely refactored as '$i >>= 2'.">$i = $i >> 2</weak_warning>;
    <weak_warning descr="[EA] Can be safely refactored as '$i %= 2'.">$i = $i % 2</weak_warning>;

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

    <weak_warning descr="[EA] Can be safely refactored as '$i .= '=' . $i'.">$i = $i . '=' . $i</weak_warning>;
    <weak_warning descr="[EA] Can be safely refactored as '$i .= '2' . '2''.">$i = $i . '2' . '2'</weak_warning>;
    <weak_warning descr="[EA] Can be safely refactored as '$i += 2 + 2'.">$i = $i + 2 + 2</weak_warning>;
    <weak_warning descr="[EA] Can be safely refactored as '$i *= 2 * 2'.">$i = $i * 2 * 2</weak_warning>;

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