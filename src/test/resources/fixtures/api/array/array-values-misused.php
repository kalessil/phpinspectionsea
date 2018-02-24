<?php

function cases_holder() {
    return [
        in_array('...', <warning descr="'array_values(...)' is not making any sense here (just search in it's argument).">array_values([])</warning>),
        in_array('...', array_values()),

        count(<warning descr="'array_values(...)' is not making any sense here (just count it's argument).">array_values([])</warning>),
        count(array_values()),

        str_replace(array_keys([]), <warning descr="'array_values(...)' is not making any sense here (just use it's argument).">array_values([])</warning>, '...'),
        str_replace(array_values([]), array_keys([]), '...'),
    ];
}