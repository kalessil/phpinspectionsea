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