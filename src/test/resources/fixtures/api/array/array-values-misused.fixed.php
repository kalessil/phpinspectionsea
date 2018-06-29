<?php

function cases_holder() {
    foreach ([] as $value) {}
    foreach (array_values([]) as $key => $value) {}

    return [
        in_array('...', []),
        in_array('...', array_values()),

        array_combine([], []),
        array_combine([], []),
        array_combine([], array_values()),
        array_combine(array_values(), []),

        array_column([], '...'),
        array_column(array_values(), '...'),

        array_values([]),
        array_values(array_values()),

        count([]),
        count(array_values()),

        str_replace(array_keys([]), [], '...'),
        str_replace(array_values([]), array_keys([]), '...'),

        array_values(array_slice([], 0)),

        array_column([], 'index'),
        array_values(array_column([], 'index', 'key')),
    ];
}