<?php

    $x = intval(1 / 2);      // <- reported
    $x = intval(++$z);       // <- reported
    $x = intval($z++);       // <- reported

    $x = intval($z ? 1 : 0); // <- reported
    $x = intval($z ?: 0);    // <- reported
    $x = intval($z ?? 0);    // <- reported

    $x = intval($y);         // <- reported
    $x = floatval($y);       // <- reported