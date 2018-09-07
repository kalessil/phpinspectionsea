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

        implode('', []),
        implode('', array_values()),

        count([]),
        count(array_values()),

        str_replace(array_keys([]), [], '...'),
        str_replace(array_values([]), array_keys([]), '...'),

        preg_replace(array_keys([]), [], '...'),
        preg_replace(array_values([]), array_keys([]), '...'),

        array_slice([], 0),
        array_slice([], 0, 1),
        array_slice(array_values([]), 0, 1, true),
        array_slice(array_values([]), 0, 1, false),

        array_column([], 'index'),
        array_values(array_column([], 'index', 'key')),

        array_slice([], 0, 1),
        array_slice([], 0, 1, false),
        array_values(array_slice([], 0, 1, true)),
    ];
}