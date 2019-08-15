<?php

function cases_holder_isset_implicit() {
    $container = $value ?? 'default';

    /* false-positives: value or container mismatches, assignment by reference */
    $container = 'default';   if (isset($value)) { $container = trim($value); }
    $container = 'default';   if (isset($value)) { $value = $value; }
    $container = &$reference; if (isset($value)) { $container = $value; }

    $container = $value ?? 'default';

    if (isset($value)) {
        $container = $value;
    } else {
        $container = $value ?? 'default';
    }

    /* false-positives: value or container mismatches */
    if (isset($value)) { $container = trim($value); } else { $container = 'default'; }
    if (isset($value)) { $value = $value; } else { $container = 'default'; }

    return $value ?? 'default';

    /* false-positives: value mismatches */
    if (isset($value)) { return trim($value); } else { return 'default'; }

    return $value ?? 'default';

    /* false-positives: value mismatches, multi-assignments (refactoring changes semantics) */
    if (isset($value)) { return trim($value); } return 'default';
    $one = $two = 'default'; if (isset($value)) { $one = $value; }
}

function cases_holder_isset_non_implicit() {
    return [
        function ($value) {
            return $value ?? null;
        },
        function ($value) {
            return $value ?? null;
        },
    ];
}

function cases_holder_identity() {
    $container = $value ?? 'default';
    $container = $value ?? 'default';

    /* false-positive: inverted logic */
    $container = 'default'; if ($value === null) { $container = $value; }

    $container = $value ?? 'default';

    /* false-positive: mismatched value */
    if ($value !== null) { $container = trim($value); } else { $container = 'default'; }

    return $value ?? 'default';

    /* false-positive: mismatched value */
    if ($value !== null) { return trim($value); } else { return 'default'; }

    return $value ?? 'default';

    /* false-positive: mismatched value */
    if ($value !== null) { return trim($value); } return 'default';
}

function cases_holder_array_key_exists() {
    $container = $array[$key] ?? null;

    /* false-positive: no-null alternative */
    $container = 'default'; if (array_key_exists($key, $array)) { $container = $array[$key]; }

    $container = $array[$key] ?? null;

    /* false-positive: mismatched value */
    if (array_key_exists($key, $array)) { $container = trim($array[$key]); } else { $container = null; }

    return $array[$key] ?? null;

    /* false-positive: mismatched value */
    if (array_key_exists($key, $array)) { return trim($array[$key]); } else { return null; }

    return $array[$key] ?? null;

    /* false-positive: mismatched value */
    if (array_key_exists($key, $array)) { return trim($array[$key]); } return null;
}