<?php

function cases_holder() {
    return [
        array_keys([]),

        array_unique(array_keys()),
        array_unique(array_keys(), SORT_STRING),
        array_unique(array_keyz(), SORT_STRING),

        count([]),
        count(array_keys([], 'search')),

        array_keys(array_slice([], 0)),
    ];
}