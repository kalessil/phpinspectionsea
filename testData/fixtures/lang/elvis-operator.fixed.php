<?php

    $x = $y ?: 0;
    $x = $y ?: 0;
    $x = ($y) ?: 0;
    $x = ((($y))) ?: 0;

    /* false-positives */
    $x = $y ? 0 : $y;
    $x = !$y ? $y : 0;