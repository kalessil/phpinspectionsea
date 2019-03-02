<?php

function test_cases_holder(string $parameter) {
    return [
        <warning descr="'basename($parameter, '.ext')' can be used instead (reduces amount of calls).">str_replace('.ext', '', basename($parameter))</warning>,
        <warning descr="'basename($parameter, '.ext')' can be used instead (reduces amount of calls).">str_replace('.ext', '', basename($parameter), 1)</warning>,

        str_replace('.ext', ''),
        str_replace('.ext', '.tmp', basename($parameter)),
        str_replace('.ext', '', basename($parameter, '.ext')),
        str_replace('.text.', '', basename($parameter)),
    ];
}