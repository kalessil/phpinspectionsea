<?php

    $x = $y = 0;

    if ($x) {
        if ($y) { // <-- reported
            exit;
        }
    }
    if ($x && !$y) {
        if ($y) { // <-- reported
            exit;
        }
    }
    if ($x && !$y) {
        if (!$x && $y) { // <-- reported
            exit;
        }
    }

    /* no report here as we are mixing and/or operations */
    if ($x || !$y) {
        if ($y) {
            exit;
        }
    }
    if ($x) {
        if ($x || !$y) {
            exit;
        }
    }