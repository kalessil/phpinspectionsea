<?php

    <weak_warning descr="Can be safely refactored as '$i += 2'.">$i = $i + 2</weak_warning>;
    <weak_warning descr="Can be safely refactored as '$i -= 2'.">$i = $i - 2</weak_warning>;
    <weak_warning descr="Can be safely refactored as '$i *= 2'.">$i = $i * 2</weak_warning>;
    <weak_warning descr="Can be safely refactored as '$i /= 2'.">$i = $i / 2</weak_warning>;
    <weak_warning descr="Can be safely refactored as '$i .= '2''.">$i = $i . '2'</weak_warning>;
    <weak_warning descr="Can be safely refactored as '$i &= 2'.">$i = $i & 2</weak_warning>;
    <weak_warning descr="Can be safely refactored as '$i |= 2'.">$i = $i | 2</weak_warning>;
    <weak_warning descr="Can be safely refactored as '$i ^= 2'.">$i = $i ^ 2</weak_warning>;
    <weak_warning descr="Can be safely refactored as '$i <<= 2'.">$i = $i << 2</weak_warning>;
    <weak_warning descr="Can be safely refactored as '$i >>= 2'.">$i = $i >> 2</weak_warning>;
    <weak_warning descr="Can be safely refactored as '$i %= 2'.">$i = $i % 2</weak_warning>;

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

    <weak_warning descr="Can be safely refactored as '$i .= '2' . '2''.">$i = $i . '2' . '2'</weak_warning>;
    $i = $i . '2' + '2';