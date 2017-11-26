<?php

function cases_holder() {
    $a = trim('');

    $b = strtolower(trim(''));

    /* false-positive: parameter by reference */
    $c = [];
    $c = array_pop($c);

    /* false-positive: 2 and more parameters */
    $d = '';
    $d = str_replace('search', 'replace', $d);
}