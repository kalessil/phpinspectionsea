<?php

function cases_holder_isset_implicit() {
    $container = 'default';
    <weak_warning descr="'$container = $value ?? 'default'' can be used instead (reduces cognitive load).">if</weak_warning> (isset($value)) {
        $container = $value;
    }

    /* false-positives: value or container mismatches, assignment by reference */
    $container = 'default';   if (isset($value)) { $container = trim($value); }
    $container = 'default';   if (isset($value)) { $value = $value; }
    $container = &$reference; if (isset($value)) { $container = $value; }

    <weak_warning descr="'$container = $value ?? 'default'' can be used instead (reduces cognitive load).">if</weak_warning> (isset($value)) {
        $container = $value;
    } else {
        $container = 'default';
    }

    if (isset($value)) {
        $container = $value;
    } else <weak_warning descr="'$container = $value ?? 'default'' can be used instead (reduces cognitive load).">if</weak_warning> (isset($value)) {
        $container = $value;
    } else {
        $container = 'default';
    }

    /* false-positives: value or container mismatches */
    if (isset($value)) { $container = trim($value); } else { $container = 'default'; }
    if (isset($value)) { $value = $value; } else { $container = 'default'; }

    <weak_warning descr="'return $value ?? 'default'' can be used instead (reduces cognitive load).">if</weak_warning> (isset($value)) {
        return $value;
    } else {
        return 'default';
    }

    /* false-positives: value mismatches */
    if (isset($value)) { return trim($value); } else { return 'default'; }

    <weak_warning descr="'return $value ?? 'default'' can be used instead (reduces cognitive load).">if</weak_warning> (isset($value)) {
        return $value;
    }
    return 'default';

    /* false-positives: value mismatches, multi-assignments (refactoring changes semantics) */
    if (isset($value)) { return trim($value); } return 'default';
    $one = $two = 'default'; if (isset($value)) { $one = $value; }
}

function cases_holder_isset_non_implicit() {
    return [
        function ($value) {
            <weak_warning descr="'return $value ?? null' can be used instead (reduces cognitive load).">if</weak_warning> (isset($value)) {
                return $value;
            }
        },
        function ($value) {
            <weak_warning descr="'return $value ?? null' can be used instead (reduces cognitive load).">if</weak_warning> (isset($value)) {
                return $value;
            }
            return;
        },
    ];
}

function cases_holder_identity() {
    $container = 'default';
    <weak_warning descr="'$container = $value ?? 'default'' can be used instead (reduces cognitive load).">if</weak_warning> ($value !== null) {
        $container = $value;
    }
    $container = 'default';
    <weak_warning descr="'$container = $value ?? 'default'' can be used instead (reduces cognitive load).">if</weak_warning> (!($value === null)) {
        $container = $value;
    }

    /* false-positive: inverted logic */
    $container = 'default'; if ($value === null) { $container = $value; }

    <weak_warning descr="'$container = $value ?? 'default'' can be used instead (reduces cognitive load).">if</weak_warning> ($value !== null) {
        $container = $value;
    } else {
        $container = 'default';
    }

    /* false-positive: mismatched value */
    if ($value !== null) { $container = trim($value); } else { $container = 'default'; }

    <weak_warning descr="'return $value ?? 'default'' can be used instead (reduces cognitive load).">if</weak_warning> ($value !== null) {
        return $value;
    } else {
        return 'default';
    }

    /* false-positive: mismatched value */
    if ($value !== null) { return trim($value); } else { return 'default'; }

    <weak_warning descr="'return $value ?? 'default'' can be used instead (reduces cognitive load).">if</weak_warning> ($value !== null) {
        return $value;
    }
    return 'default';

    /* false-positive: mismatched value */
    if ($value !== null) { return trim($value); } return 'default';
}

function cases_holder_array_key_exists() {
    $container = null;
    <weak_warning descr="'$container = $array[$key] ?? null' can be used instead (reduces cognitive load).">if</weak_warning> (array_key_exists($key, $array)) {
        $container = $array[$key];
    }

    /* false-positive: no-null alternative */
    $container = 'default'; if (array_key_exists($key, $array)) { $container = $array[$key]; }

    <weak_warning descr="'$container = $array[$key] ?? null' can be used instead (reduces cognitive load).">if</weak_warning> (array_key_exists($key, $array)) {
        $container = $array[$key];
    } else {
        $container = null;
    }

    /* false-positive: mismatched value */
    if (array_key_exists($key, $array)) { $container = trim($array[$key]); } else { $container = null; }

    <weak_warning descr="'return $array[$key] ?? null' can be used instead (reduces cognitive load).">if</weak_warning> (array_key_exists($key, $array)) {
        return $array[$key];
    } else {
        return null;
    }

    /* false-positive: mismatched value */
    if (array_key_exists($key, $array)) { return trim($array[$key]); } else { return null; }

    <weak_warning descr="'return $array[$key] ?? null' can be used instead (reduces cognitive load).">if</weak_warning> (array_key_exists($key, $array)) {
        return $array[$key];
    }
    return null;

    /* false-positive: mismatched value */
    if (array_key_exists($key, $array)) { return trim($array[$key]); } return null;
}