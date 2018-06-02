<?php

function cases_holder() {
    foreach ([] as $value) {}
    foreach (array_values([]) as $key => $value) {}

    return [
        in_array('...', []),
        in_array('...', array_values()),

        count([]),
        count(array_values()),

        str_replace(array_keys([]), [], '...'),
        str_replace(array_values([]), array_keys([]), '...'),

        array_values(array_slice([], 0)),
    ];
}