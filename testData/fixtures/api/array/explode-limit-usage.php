<?php

function cases_holder_positive_limit() {
    $skip = explode('/', '...');
    $target = <warning descr="'explode('/', '...', 2)' could be used here (only the first part has been used).">explode('/', '...')</warning>;

    list($first,) = <warning descr="'explode('/', '...', 2)' could be used here (only the first part has been used).">explode('/', '...')</warning>;
    list($first) = <warning descr="'explode('/', '...', 2)' could be used here (only the first part has been used).">explode('/', '...')</warning>;
    list($first, ,) = explode('/', '...');

    return [
        <warning descr="'explode('/', '...', 2)' could be used here (only the first part has been used).">explode('/', '...')</warning>[0],
        $target[0],

        explode('/', '...')[1],
        explode('/', '...'),
        $skip[1],

        current(<warning descr="'explode('/', '...', 2)' could be used here (only the first part has been used).">explode('/', '...')</warning>),
    ];
}

function cases_holder_negative_limit($argument) {
    $parts = <warning descr="'explode(':', $argument, -1)' could be used here (following 'array_pop(...)' call to be dropped then).">explode(':', $argument)</warning>;
    array_pop($parts);
    array_pop($parts);

    $fragments = <warning descr="'explode(':', $argument, -2)' could be used here (following 'array_pop(...)' call to be dropped then).">explode(':', $argument, -1)</warning>;
    array_pop($fragments);

    return [$parts, $fragments];
}