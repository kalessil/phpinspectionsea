<?php

function cases_holder() {
    $array = [];

    return [
        <weak_warning descr="[EA] Can be replaced by '!array_is_list($array)' (improves maintainability).">array_keys($array) != range(0, count($array) - 1)</weak_warning>,
        <weak_warning descr="[EA] Can be replaced by '!array_is_list($array)' (improves maintainability).">array_keys($array) !== range(0, count($array) - 1)</weak_warning>,
        <weak_warning descr="[EA] Can be replaced by 'array_is_list($array)' (improves maintainability).">array_keys($array) == range(0, count($array) - 1)</weak_warning>,
        <weak_warning descr="[EA] Can be replaced by 'array_is_list($array)' (improves maintainability).">array_keys($array) === range(0, count($array) - 1)</weak_warning>,

        <weak_warning descr="[EA] Can be replaced by '!array_is_list($array)' (improves maintainability).">array_values($array) !== $array</weak_warning>,
        <weak_warning descr="[EA] Can be replaced by '!array_is_list($array)' (improves maintainability).">array_values($array) != $array</weak_warning>,
        <weak_warning descr="[EA] Can be replaced by 'array_is_list($array)' (improves maintainability).">array_values($array) === $array</weak_warning>,
        <weak_warning descr="[EA] Can be replaced by 'array_is_list($array)' (improves maintainability).">array_values($array) == $array</weak_warning>,

        /* false-positives */
        array_keys($array) != range(0, count([]) - 1),
        array_values($array) !== [],
    ];
}