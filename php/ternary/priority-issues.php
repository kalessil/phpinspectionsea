<?php

    /* sort this out; if ternary already wrapped with parenthesises then report nothing */
    echo $a & $b ? 0 : 1;
    echo $a | $b ? 0 : 1;
    echo $a - $b ? 0 : 1;
    echo $a + $b ? 0 : 1;
    echo $a / $b ? 0 : 1;
    echo $a * $b ? 0 : 1;
    echo $a % $b ? 0 : 1;
    echo $a ^ $b ? 0 : 1;
    echo $a > $b ? 0 : 1;
    echo $a >= $b ? 0 : 1;
    echo $a < $b ? 0 : 1;
    echo $a <= $b ? 0 : 1;
    echo $a && $b ? 0 : 1;
    echo $a || $b ? 0 : 1;
    echo $a and $b ? 0 : 1;
    echo $a or  $b ? 0 : 1;
    echo $a == $b ? 0 : 1;
    echo $a != $b ? 0 : 1;
    echo $a === $b ? 0 : 1;
    echo $a !== $b ? 0 : 1;

    echo (($a !== $b) ? 0 : 1); // <- ternary is in () => skip processing
    echo ($a !== $b) ? 0 : 1;   // <- if parent is still binary => we need to check this

    /* bugs */
    echo ($b ? 0 : 0);

    /* nested ternaries */
    echo $a ? $a : ($b ? $b : null);
    echo !$a ? ($b ? $b : null) : $a;
    echo ($b ? $b : null) ? !$a : $a;

    /* false-positives */