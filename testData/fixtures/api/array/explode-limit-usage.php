<?php

function cases_holder() {
    $skip = explode('/', '...');
    $target = <warning descr="'explode('/', '...', 2)' would fit more here (consumes less cpu and memory resources).">explode('/', '...')</warning>;

    return [
        <warning descr="'explode('/', '...', 2)' would fit more here (consumes less cpu and memory resources).">explode('/', '...')</warning>[0],
        $target[0],

        explode('/', '...')[1],
        explode('/', '...'),
        $skip[1],
    ];
}