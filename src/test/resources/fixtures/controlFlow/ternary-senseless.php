<?php

    $z = <weak_warning descr="Can be replaced with '$y'">$x == $y ? $x : $y</weak_warning>;
    $z = <weak_warning descr="Can be replaced with '$y'">$x === $y ? $x : $y</weak_warning>;

    /* false-positives */
    $z = $x != $y ? $x : $y;
    $z = $x !== $y ? $x : $y;
    $z = $x ? $x : $y;