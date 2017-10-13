<?php

function cases_holder($x, $y) {
    $array = compact('x');
    $array = compact('x', 'y');

    $array = array('_x' => $x);
    $array = array('x'  => $array['x']);
}