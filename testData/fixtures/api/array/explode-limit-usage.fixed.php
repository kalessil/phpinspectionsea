<?php

function cases_holder_positive_limit() {
    $skip = explode('/', '...');
    $target = explode('/', '...', 2);

    list($first,) = explode('/', '...', 2);
    list($first) = explode('/', '...', 2);
    list($first, ,) = explode('/', '...');

    return [
        explode('/', '...', 2)[0],
        $target[0],

        explode('/', '...')[1],
        explode('/', '...'),
        $skip[1],

        current(explode('/', '...', 2)),
    ];
}

function cases_holder_negative_limit($argument) {
    $parts = explode(':', $argument, -1);
    array_pop($parts);

    $fragments = explode(':', $argument, -2);

    return [$parts, $fragments];
}