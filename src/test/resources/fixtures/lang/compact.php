<?php

function cases_holder($x, $y) {
    $array = <weak_warning descr="'compact('x', 'y')' can be used instead instead (improves maintainability).">[</weak_warning>'x' => $x, 'y' => $y];

    $array = array('x' => $x);
    $array = array('_x' => $x);
    $array = array('x'  => $array['x']);
}