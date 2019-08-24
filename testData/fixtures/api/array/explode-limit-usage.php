<?php

function cases_holder() {
    $skip = explode('/', '...');
    $target = explode('/', '...');

    return [
        explode('/', '...')[0],
        $target[0],

        explode('/', '...')[1],
        explode('/', '...'),
        $skip[1],
    ];
}