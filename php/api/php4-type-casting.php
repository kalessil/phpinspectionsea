<?php

    $x = intval(1 / 2);
    $x = intval(++$z);
    $x = intval($z++);

    $x = intval($z ? 1 : 0);
    $x = intval($z ?: 0);
    $x = intval($z ?? 0);

    $x = intval($y);