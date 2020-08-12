<?php

function cases_holder() {
    return [
        str_starts_with('haystack', 'needle'),
        str_starts_with('haystack', 'needle'),
        str_starts_with('haystack', 'needle'),

        !str_starts_with('haystack', 'needle'),
        !str_starts_with('haystack', 'needle'),
        !str_starts_with('haystack', 'needle'),

        strpos('haystack', 'needle', 0) === 0,
        mb_strpos('haystack', 'needle', 0) === 0,
    ];
}