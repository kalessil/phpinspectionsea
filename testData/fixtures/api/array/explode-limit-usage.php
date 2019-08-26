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