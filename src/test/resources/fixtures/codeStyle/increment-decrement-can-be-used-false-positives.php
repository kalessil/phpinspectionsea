<?php

    $x += 2;
    $x -= 2;

    $x = $x + 2;
    $x = 2 + $x;

    $x = 1 - $x;

    $x = $x - 2;
    $x = 2 - $x;

    /* @var $x string */
    $x[0] += 1;
    $x[0] -= 1;

    /* @var $x \ArrayAccess */
    $x[0] += 1;
    $x[0] -= 1;