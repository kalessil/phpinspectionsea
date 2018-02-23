<?php

function cases_holder() {
    return [
        in_array('...', []),
        in_array('...', array_values()),

        count([]),
        count(array_values()),
    ];
}