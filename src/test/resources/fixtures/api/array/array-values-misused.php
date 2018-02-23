<?php

function cases_holder() {
    return [
        in_array('...', <warning descr="'array_keys(...)' is not making any sense here (just search in it's argument).">array_values([])</warning>),
        in_array('...', array_values()),

        count(<warning descr="'array_keys(...)' is not making any sense here (just count it's argument)">array_values([])</warning>),
        count(array_values()),
    ];
}