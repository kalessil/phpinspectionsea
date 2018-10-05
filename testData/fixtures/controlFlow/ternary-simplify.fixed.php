<?php

    $x = $x > 0;
    $x = $x <= 0;
    $x = (bool)($x & 0);
    $x = !($x & 0);
    $x = ($x && $y);
    $x = !($x && $y);

    $x = $x > 0;
    $x = $x > 0;

    $x = $x > 0 ? true : null;
    $x = is_numeric($x) ? false : true;


    $x = empty($x);
    $x = !empty($x);
    $x = !empty($x);
    $x = empty($x);

    $x = empty($x) ? true : null;

    $x = isset($x);
    $x = !isset($x);
    $x = isset($x);

    $x = isset($x) ? false : null;