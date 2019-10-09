<?php

function cases_holder() {
    $found   = false;
    $missing = true;
    foreach ([] as $value) {
        if ($value) {
            $found = true;
            $missing = false;
            break;
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
}