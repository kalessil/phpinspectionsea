<?php

function cases_holder() {
    foreach (<warning descr="'array_values(...)' is not making any sense here (just use it's argument).">array_values([])</warning> as $value) {}
    foreach (array_values([]) as $key => $value) {}

    return [
        in_array('...', <warning descr="'array_values(...)' is not making any sense here (just search in it's argument).">array_values([])</warning>),
        in_array('...', array_values()),

        count(<warning descr="'array_values(...)' is not making any sense here (just count it's argument).">array_values([])</warning>),
        count(array_values()),

        str_replace(array_keys([]), <warning descr="'array_values(...)' is not making any sense here (just use it's argument).">array_values([])</warning>, '...'),
        str_replace(array_values([]), array_keys([]), '...'),

        <warning descr="'array_values(array_slice([], 0))' is making more sense here (reduces amount of processed elements).">array_slice(array_values([]), 0)</warning>,
    ];
}