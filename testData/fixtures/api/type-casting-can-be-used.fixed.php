<?php

function cases_holder() {
    $x = (int)$y;
    $x = (float)$y;
    $x = (string)$y;
    $x = (bool)$y;

    $x = (int)($y ?? 1);
    $x = (int)($y ?: 1);

    $x = intval(1 / 2, 16);

    $x = (int)$x;
    $x = (float)$x;
    $x = (bool)$x;
    $x = (string)$x;
    $x = (array)$x;

    settype($x, $x);
    settype($x, 'whatever');

    $x = (string)($y);
    $x = (string)$y;

    $x = " $y";
    $x = "$y ";
    $x = "{$y}{$y}";

    $x = $x * 1;
    $x = 1 * $x;

    $x = $x * -1;
}