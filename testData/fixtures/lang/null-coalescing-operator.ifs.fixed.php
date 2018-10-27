<?php

function cases_holder() {
    $container = $value ?? 'default';

    /* false-positives: value or container mismatches */
    $container = 'default';
    if (isset($value)) {
        $container = trim($value);
    }
    $container = 'default';
    if (isset($value)) {
        $value = $value;
    }

    $container = $value ?? 'default';

    return $value ?? 'default';

    return $value ?? 'default';
}