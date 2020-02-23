<?php

    /* !... equivalent cases */
    $x = !$x5;
    $x = !$x3;
    $x = !($x7 || $x7);


    /* (bool)... equivalent cases */
    $x = (bool)$x4;
    $x = (bool)$x2;
    $x = (bool)($x6 || $x6);

    /* parentheses handling */
    $x = (bool)$x2;
    $x = !$x2;

    /* false-positives */
    $x = !$x1;