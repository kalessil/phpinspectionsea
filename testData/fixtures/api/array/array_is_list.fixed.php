<?php

function cases_holder() {
    $array = [];

    return [
        !array_is_list($array),
        !array_is_list($array),
        array_is_list($array),
        array_is_list($array),

        !array_is_list($array),
        !array_is_list($array),
        array_is_list($array),
        array_is_list($array),

        /* false-positives */
        array_keys($array) != range(0, count([]) - 1),
        array_values($array) !== [],
    ];
}