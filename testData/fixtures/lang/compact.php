<?php

function cases_holder($x, $y) {
    $array = <weak_warning descr="[EA] 'compact('x', 'y')' can be used instead (improves maintainability).">[</weak_warning>'x' => $x, 'y' => $y];

    $array = array('x' => $x);
    $array = array('x' => $y, 'y' => $x);
    $array = array('x' => $x, 'y' => (array) $y);

    // False-positive: php array destructuring
    ['x' => $x, 'y' => $y] = $array;
}