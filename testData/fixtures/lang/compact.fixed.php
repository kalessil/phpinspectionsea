<?php

function cases_holder($x, $y) {
    $array = compact('x', 'y');

    $array = array('x' => $x);
    $array = array('x' => $y, 'y' => $x);
    $array = array('x' => $x, 'y' => (array) $y);

    // False-positive: php array destructuring
    ['x' => $x, 'y' => $y] = $array;
}