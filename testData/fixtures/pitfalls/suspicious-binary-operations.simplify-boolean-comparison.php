<?php

function cases_holder($x, $y, $z, $t) {
    if (($x && $y) || (<error descr="'(X && Y) || (!X && !Y)' -> '(bool) X === (bool) Y'">!$x && !$y</error>)) {}
    if (($x && !$y) || (<error descr="'(X && !Y) || (!X && Y)' -> '(bool) X !== (bool) Y'">!$x && $y</error>)) {}

    if (($x && !$y) || ($z && $t) || (<error descr="'(X && !Y) || (!X && Y)' -> '(bool) X !== (bool) Y'">!$x && $y</error>)) {}
    if (($x === true && $y) || (<error descr="'(X && Y) || (!X && !Y)' -> '(bool) X === (bool) Y'">$x === false && !$y</error>)) {}
    if (($x !== false && $y) || (<error descr="'(X && Y) || (!X && !Y)' -> '(bool) X === (bool) Y'">$x !== true && !$y</error>)) {}

    /* false-positives: empty()-handling */
    if ((!empty($x) && $y) || ($x && !$y)) {}
    if (($x && $y) || (empty($x) && !$y)) {}
}