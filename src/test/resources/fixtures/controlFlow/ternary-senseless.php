<?php

    <weak_warning descr="Can be replaced with '$y'.">$x === $y ? $x : $y</weak_warning>;
    <weak_warning descr="Can be replaced with '$x'.">$x !== $y ? $x : $y</weak_warning>;

    <weak_warning descr="Can be replaced with '$x'.">$x === 0 ? 0 : $x</weak_warning>;
    <weak_warning descr="Can be replaced with '$x'.">$x !== 0 ? $x : 0</weak_warning>;
    <weak_warning descr="Can be replaced with '0'.">$x === 0 ? $x : 0</weak_warning>;
    <weak_warning descr="Can be replaced with '0'.">$x !== 0 ? 0 : $x</weak_warning>;

    <weak_warning descr="Can be replaced with '$x'.">$x === '' ? '' : $x</weak_warning>;

    /* false-positives */
    $x == $y ? $x : $y;
    $x ? $x : $y;