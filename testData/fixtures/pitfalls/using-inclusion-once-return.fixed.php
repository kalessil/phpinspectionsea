<?php

    $x = require $x;
    $y = require $x &&
        require $y;
    if (require $x) {
        return (require $x);
    }

    /* false-positives */
    require_once $x;
    return (require __DIR__ . 'semicolons.php');