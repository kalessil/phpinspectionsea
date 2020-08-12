<?php

function cases_holder() {
    return [
        <weak_warning descr="[EA] Can be replaced by 'str_contains('haystack', 'needle')' (improves maintainability).">substr_count('haystack', 'needle') > 0</weak_warning>,
        <weak_warning descr="[EA] Can be replaced by 'str_contains('haystack', 'needle')' (improves maintainability).">substr_count('haystack', 'needle')</weak_warning> || false,
        <weak_warning descr="[EA] Can be replaced by 'str_contains('haystack', 'needle')' (improves maintainability).">strpos('haystack', 'needle') !== false</weak_warning>,
        <weak_warning descr="[EA] Can be replaced by 'str_contains('haystack', 'needle')' (improves maintainability).">mb_strpos('haystack', 'needle') !== false</weak_warning>,

        <weak_warning descr="[EA] Can be replaced by '! str_contains('haystack', 'needle')' (improves maintainability).">substr_count('haystack', 'needle') === 0</weak_warning>,
        ! <weak_warning descr="[EA] Can be replaced by 'str_contains('haystack', 'needle')' (improves maintainability).">substr_count('haystack', 'needle')</weak_warning>,
        <weak_warning descr="[EA] Can be replaced by '! str_contains('haystack', 'needle')' (improves maintainability).">strpos('haystack', 'needle') === false</weak_warning>,
        <weak_warning descr="[EA] Can be replaced by '! str_contains('haystack', 'needle')' (improves maintainability).">mb_strpos('haystack', 'needle') === false</weak_warning>,

        strpos('haystack', 'needle', 0) === false,
        mb_strpos('haystack', 'needle', 0) === false,
    ];
}