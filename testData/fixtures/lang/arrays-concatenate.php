<?php

    $x = [];
    $y = [];

    $z = $x <warning descr="Perhaps array_merge/array_replace can be used instead. Feel free to disable the inspection if '+' is intended.">+</warning> $y;
    $z <warning descr="Perhaps array_merge/array_replace can be used instead. Feel free to disable the inspection if '+' is intended.">+=</warning> $y;

    /* false-positives: implicit arrays, obvious types and behaviour */
    $a = [] + $z;
    $b = [] + $x + $y;
    $c = $y + $x + [];
    $d = $z + [];
    $z += [];

    /* false-positives: summing up with non-array types */
    $e = $x + '...';
    $x += '...';