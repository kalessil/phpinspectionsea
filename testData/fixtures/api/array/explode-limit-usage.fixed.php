<?php

function cases_holder() {
    $skip = explode('/', '...');
    $target = explode('/', '...', 2);

    return [
        explode('/', '...', 2)[0],
        $target[0],

        explode('/', '...')[1],
        explode('/', '...'),
        $skip[1],
    ];
}

function cases_holder_negative_limit($argument) {
    $parts = explode(':', $argument, -2);

    return $parts;
}