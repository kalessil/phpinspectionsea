<?php

function cases_holder($x, $y) {
    $array = compact('x', 'y');

    $array = array('x' => $x);
    $array = array('_x' => $x);
    $array = array('x'  => $array['x']);
}