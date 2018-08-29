<?php

    <warning descr="Can be replaced with '$y'.">$x === $y ? $x : $y</warning>;
    <warning descr="Can be replaced with '$x'.">$x !== $y ? $x : $y</warning>;

    <warning descr="Can be replaced with '$x'.">$x === 0 ? 0 : $x</warning>;
    <warning descr="Can be replaced with '$x'.">$x !== 0 ? $x : 0</warning>;
    <warning descr="Can be replaced with '0'.">$x === 0 ? $x : 0</warning>;
    <warning descr="Can be replaced with '0'.">$x !== 0 ? 0 : $x</warning>;

    <warning descr="Can be replaced with '$x'.">$x === '' ? '' : $x</warning>;

    /* false-positives */
    $x == $y ? $x : $y;
    $x ? $x : $y;