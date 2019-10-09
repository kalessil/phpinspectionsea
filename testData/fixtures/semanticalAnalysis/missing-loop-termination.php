<?php

function cases_holder() {
    $found   = false;
    $missing = true;
    <warning descr="It seems the loop termination is missing, please place 'break;' at a proper place.">foreach</warning> ([] as $value) {
        if ($value) {
            $found   = true;
            $missing = false;
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