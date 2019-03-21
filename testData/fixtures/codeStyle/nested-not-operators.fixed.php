<?php

    /* !... equivalent cases */
    $x = !$x5;
    $x = !$x3;

    /* (bool)... equivalent cases */
    $x = (bool)$x4;
    $x = (bool)$x2;

    /* parentheses handling */
    $x = (bool)$x2;
    $x = !$x2;

    /* false-positives */
    $x = !$x1;