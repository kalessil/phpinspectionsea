<?php

function cases_holder() {
    return [
        <weak_warning descr="[EA] Can be replaced by 'str_starts_with('haystack', 'needle')' (improves maintainability).">strncmp('haystack', 'needle', strlen('needle')) === 0</weak_warning>,
        <weak_warning descr="[EA] Can be replaced by 'str_starts_with('haystack', 'needle')' (improves maintainability).">substr_compare('haystack', 'needle', 0, strlen('needle')) === 0</weak_warning>,
        <weak_warning descr="[EA] Can be replaced by 'str_starts_with('haystack', 'needle')' (improves maintainability).">strpos('haystack', 'needle') === 0</weak_warning>,
        <weak_warning descr="[EA] Can be replaced by 'str_starts_with('haystack', 'needle')' (improves maintainability).">mb_strpos('haystack', 'needle') === 0</weak_warning>,

        <weak_warning descr="[EA] Can be replaced by '! str_starts_with('haystack', 'needle')' (improves maintainability).">strncmp('haystack', 'needle', strlen('needle')) !== 0</weak_warning>,
        <weak_warning descr="[EA] Can be replaced by '! str_starts_with('haystack', 'needle')' (improves maintainability).">substr_compare('haystack', 'needle', 0, strlen('needle')) !== 0</weak_warning>,
        <weak_warning descr="[EA] Can be replaced by '! str_starts_with('haystack', 'needle')' (improves maintainability).">strpos('haystack', 'needle') !== 0</weak_warning>,
        <weak_warning descr="[EA] Can be replaced by '! str_starts_with('haystack', 'needle')' (improves maintainability).">mb_strpos('haystack', 'needle') !== 0</weak_warning>,

        strpos('haystack', 'needle', 0) === 0,
        mb_strpos('haystack', 'needle', 0) === 0,
    ];
}