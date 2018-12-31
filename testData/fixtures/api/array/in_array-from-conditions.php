<?php

function cases_holder($argument) {
    return [
        $argument == 0 || $argument == '...' || <warning descr="It's possible to use 'in_array(..., [...])' here (reduces cognitive load).">$argument == PHP_INT_MAX</warning>,
        $argument === 0 || $argument === '...' || <warning descr="It's possible to use 'in_array(..., [...])' here (reduces cognitive load).">$argument === PHP_INT_MAX</warning>,
        $argument != 0 && $argument != '...' && <warning descr="It's possible to use '!in_array(..., [...])' here (reduces cognitive load).">$argument != PHP_INT_MAX</warning>,
        $argument !== 0 && $argument !== '...' && <warning descr="It's possible to use '!in_array(..., [...])' here (reduces cognitive load).">$argument !== PHP_INT_MAX</warning>,

        trim($argument) == 0 || <warning descr="It's possible to use '!in_array(..., [...])' here (reduces cognitive load).">trim($argument) == '...'</warning>,
        trim($argument) != 0 && <warning descr="It's possible to use '!in_array(..., [...])' here (reduces cognitive load).">trim($argument) != '...'</warning>,

        /* false-positives */
        $argument != 0 || $argument != 1 || $argument != PHP_INT_MAX,
        $argument != 0 || $argument == 1 || $argument == PHP_INT_MAX,
        $argument == 0 && $argument == 1 && $argument == PHP_INT_MAX,
        $argument == 0 && $argument != 1 && $argument != PHP_INT_MAX,
    ];
}