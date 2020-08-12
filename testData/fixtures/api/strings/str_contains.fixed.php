<?php

function cases_holder() {
    return [
        str_contains('haystack', 'needle'),
        str_contains('haystack', 'needle') || false,
        str_contains('haystack', 'needle'),
        str_contains('haystack', 'needle'),

        !str_contains('haystack', 'needle'),
        !str_contains('haystack', 'needle'),
        !str_contains('haystack', 'needle'),
        !str_contains('haystack', 'needle'),

        strpos('haystack', 'needle', 0) === false,
        mb_strpos('haystack', 'needle', 0) === false,
    ];
}