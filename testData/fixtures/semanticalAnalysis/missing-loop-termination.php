<?php

function cases_holder() {
    $found   = false;
    $missing = true;
    <warning descr="[EA] It seems the loop termination is missing, please place 'break;' at a proper place.">foreach</warning> ([] as $value) {
        if ($value) {
            $found = true;
            $missing = false;
        }
    }

    <warning descr="[EA] It seems the loop termination is missing, please place 'break;' at a proper place.">foreach</warning> ([] as $value) {
        if ($value) {
            $found = true;
            continue;
        }
    }

    foreach ([] as $value) {
        if ($value) {
            $found = true;
            break;
        }
    }

    foreach ([] as $value) {
        if ($value) {
            $element = $value;
            $found   = true;
        }
    }

    <warning descr="[EA] It seems the loop termination is missing, please place 'return ...;' at a proper place.">foreach</warning> ([] as $value) {
        if ($value) {
            $found = true;
        }
    }
    return $found;
}