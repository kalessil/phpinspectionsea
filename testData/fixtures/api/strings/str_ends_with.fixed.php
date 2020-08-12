<?php

return [
    str_ends_with('haystack', 'needle'),
    str_ends_with('haystack', 'needle'),
    str_ends_with('haystack', 'needle'),
    str_ends_with('haystack', 'needle'),
    str_ends_with('haystack', 'needle'),

    !str_ends_with('haystack', 'needle'),
    !str_ends_with('haystack', 'needle'),
    !str_ends_with('haystack', 'needle'),
    !str_ends_with('haystack', 'needle'),
    !str_ends_with('haystack', 'needle'),

    substr('haystack', -strlen('needle')) !== '...',
    substr('haystack', -strlen('needle'), 1) !== 'needle',
    mb_substr('haystack', - mb_strlen('needle', '...')) === 'needle',
];