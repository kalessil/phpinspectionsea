<?php

// array_slice(explode('/', ...), 0, -N) -> explode('/', ..., -number)
// list($firstKey[,]) = explode('/', ...);  amount of commas + 1

function cases_holder_positive_limit() {
    $skip = explode('/', '...');
    $target = <warning descr="'explode('/', '...', 2)' could be used here (only the first part has been used).">explode('/', '...')</warning>;

    return [
        <warning descr="'explode('/', '...', 2)' could be used here (only the first part has been used).">explode('/', '...')</warning>[0],
        $target[0],

        explode('/', '...')[1],
        explode('/', '...'),
        $skip[1],

        <warning descr="'explode('/', '...', 2)[0]' could be used here (only the first part has been used).">current(explode('/', '...'))</warning>,
        <warning descr="'explode('/', '...', 2)[0]' could be used here (only the first part has been used).">array_slice(explode('/', '...'), 0, 1)</warning>,
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