<?php

$i = 0;

$i = $i + 2;   // <- reported
$i = 2 + $i;

$i = $i - 2;   // <- reported
$i = 2 - $i;

$i = $i * 2;   // <- reported
$i = 2 * $i;

$i = $i / 2;   // <- reported
$i = 2 / $i;

$i = $i . '2'; // <- reported
$i = '2' . $i;

$i = $i & 2;   // <- reported
$i = 2 & $i;

$i = $i | 2;   // <- reported
$i = 2 | $i;

$i = $i ^ 2;   // <- reported
$i = 2 ^ $i;

$i = $i << 2;  // <- reported
$i = 2 << $i;

$i = $i >> 2;  // <- reported
$i = 2 >> $i;
