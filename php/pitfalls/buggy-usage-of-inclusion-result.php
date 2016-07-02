<?php

    require_once __DIR__ . 'semicolons.php';

    $x = require_once __DIR__ . 'semicolons.php';                                            // <- reported
    $y = require_once __DIR__ . 'semicolons.php' && require_once __DIR__ . 'semicolons.php'; // <- reported
    if (require_once __DIR__ . 'semicolons.php') {                                           // <- reported
        return (require_once __DIR__ . 'semicolons.php');                                    // <- reported
    }

    return (require __DIR__ . 'semicolons.php');