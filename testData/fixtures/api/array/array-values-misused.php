<?php

function cases_holder() {
    foreach (<warning descr="'array_values(...)' is not making any sense here (just use it's argument).">array_values([])</warning> as $value) {}
    foreach (array_values([]) as $key => $value) {}

    return [
        in_array('...', <warning descr="'array_values(...)' is not making any sense here (just search in it's argument).">array_values([])</warning>),
        in_array('...', array_values()),

        array_combine([], <warning descr="'array_values(...)' is not making any sense here (just use it's argument).">array_values([])</warning>),
        array_combine(<warning descr="'array_values(...)' is not making any sense here (just use it's argument).">array_values([])</warning>, []),
        array_combine([], array_values()),
        array_combine(array_values(), []),

        array_column(<warning descr="'array_values(...)' is not making any sense here (just use it's argument).">array_values([])</warning>, '...'),
        array_column(array_values(), '...'),

        array_values(<warning descr="'array_values(...)' is not making any sense here (just use it's argument).">array_values([])</warning>),
        array_values(array_values()),

        implode('', <warning descr="'array_values(...)' is not making any sense here (just use it's argument).">array_values([])</warning>),
        implode('', array_values()),

        count(<warning descr="'array_values(...)' is not making any sense here (just count it's argument).">array_values([])</warning>),
        count(array_values()),

        str_replace(array_keys([]), <warning descr="'array_values(...)' is not making any sense here (just use it's argument).">array_values([])</warning>, '...'),
        str_replace(array_values([]), array_keys([]), '...'),

        preg_replace(array_keys([]), <warning descr="'array_values(...)' is not making any sense here (just use it's argument).">array_values([])</warning>, '...'),
        preg_replace(array_values([]), array_keys([]), '...'),

        array_slice(<warning descr="'array_values(...)' is not making any sense here (just use it's argument).">array_values([])</warning>, 0),
        array_slice(<warning descr="'array_values(...)' is not making any sense here (just use it's argument).">array_values([])</warning>, 0, 1),
        array_slice(array_values([]), 0, 1, true),
        array_slice(array_values([]), 0, 1, false),

        <warning descr="'array_values(...)' is not making any sense here (just use it's argument).">array_values(array_column([], 'index'))</warning>,
        array_values(array_column([], 'index', 'key')),

        <warning descr="'array_values(...)' is not making any sense here (just use it's argument).">array_values(array_slice([], 0, 1))</warning>,
        <warning descr="'array_values(...)' is not making any sense here (just use it's argument).">array_values(array_slice([], 0, 1, false))</warning>,
        array_values(array_slice([], 0, 1, true)),
    ];
}