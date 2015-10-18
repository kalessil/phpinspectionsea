<?php

$x = 0;
$x += 1;
$x += 2;     //shall not warn
$x = $x + 1;
$x = $x + 2; //shall not warn
$x = 1 + $x;

$x = 0;
$x -= 1;
$x -= 2;     //shall not warn
$x = $x - 1;
$x = $x - 2; //shall not warn
$x = 1 - $x; //shall not warn

/* @var $x string */
$x[0] += 1;

/* @var $x \ArrayAccess */
$x[0] += 1;

