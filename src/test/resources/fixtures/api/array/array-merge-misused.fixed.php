<?php

function cases_holder($x) {
    return [
        array_merge([], [], [], []),

        array_push($x, 0),
        array_push($x, 0, 1, 2),
        $x = array_merge($x, []),
        $x = array_merge($x, ['key' => 'value']),
    ];
}