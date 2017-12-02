<?php

function cases_holder() {
    return [
        <warning descr="'array_unique(...)' is not making any sense here (array keys are unique).">array_unique(array_keys([]))</warning>,
        <warning descr="'array_unique(...)' is not making any sense here (array keys are unique).">array_unique(array_keys())</warning>,

        array_unique(array_keys(), SORT_STRING),
        array_unique(array_keyz(), SORT_STRING)
    ];
}