<?php

function cases_holder() {
    $skip = explode('/', '...');
    $target = <warning descr="Perhaps 'explode('/', '...', 2)' can be used here, as only the first part has been used.">explode('/', '...')</warning>;

    return [
        <warning descr="Perhaps 'explode('/', '...', 2)' can be used here, as only the first part has been used.">explode('/', '...')</warning>[0],
        $target[0],

        explode('/', '...')[1],
        explode('/', '...'),
        $skip[1],
    ];
}

function cases_holder_negative_limit($argument) {
    $parts = <warning descr="Perhaps 'explode(':', $argument, -2)' can be used here (2 following 'array_pop(...)' calls can be dropped then).">explode(':', $argument)</warning>;
    array_pop($parts);
    array_pop($parts);

    return $parts;
}