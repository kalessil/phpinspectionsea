<?php

function cases_holder() {
    return [
        <warning descr="'array_unique(...)' is not making any sense here (array keys are unique).">array_unique(array_keys([]))</warning>,

        array_unique(array_keys()),
        array_unique(array_keys(), SORT_STRING),
        array_unique(array_keyz(), SORT_STRING),

        count(<warning descr="'array_keys(...)' is not making any sense here (just count it's argument).">array_keys([])</warning>),
        count(array_keys([], 'search')),

        <warning descr="'array_keys(array_slice([], 0))' is making more sense here (reduces amount of processed elements).">array_slice(array_keys([]), 0)</warning>,
    ];
}