<?php

    $x = [];
    $y = [];

    $z = $x <error descr="Perhaps array_merge/array_replace can be used instead. Feel free to disable the inspection if '+' is intended.">+</error> $y;
    $z <error descr="Perhaps array_merge/array_replace can be used instead. Feel free to disable the inspection if '+' is intended.">+=</error> $y;

    /* false-positives: implicit arrays, obvious types and behaviour */
    $a = [] + $z;
    $b = [] + $x + $y;
    $c = $y + $x + [];
    $d = $z + [];
    $z += [];