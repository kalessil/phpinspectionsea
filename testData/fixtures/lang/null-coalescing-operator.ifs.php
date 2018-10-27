<?php

    $container = 'default';
    <weak_warning descr="'$container = $value ?? 'default'' can be used instead (reduces cognitive load).">if</weak_warning> (isset($value)) {
        $container = $value;
    }

    /* false-positives: value or container mismatches */
    $container = 'default';
    if (isset($value)) {
        $container = trim($value);
    }
    $container = 'default';
    if (isset($value)) {
        $value = $value;
    }

    <weak_warning descr="'$container = $value ?? 'default'' can be used instead (reduces cognitive load).">if</weak_warning> (isset($value)) {
        $container = $value;
    } else {
        $container = 'default';
    }

    <weak_warning descr="'return $value ?? 'default'' can be used instead (reduces cognitive load).">if</weak_warning> (isset($value)) {
        return $value;
    } else {
        return 'default';
    }

    <weak_warning descr="'return $value ?? 'default'' can be used instead (reduces cognitive load).">if</weak_warning> (isset($value)) {
        return $value;
    }
    return 'default';