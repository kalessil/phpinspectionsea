<?php

function cases_holder($parameter) {
    $lambda = function ($regular, & $reference) use (& $parameter) {
        if ($parameter === null) {
            return $parameter = '...';
        }
        if ($parameter === null) {
            return $reference = '...';
        }
        if ($parameter === null) {
            <weak_warning descr="Assignment here is not making much sense.">return $regular = '...';</weak_warning>
        }
    };

    <weak_warning descr="Assignment here is not making much sense.">return $parameter = $lambda;</weak_warning>
}

function multiple_return_statements($parameter) {
    if (0 === $parameter) { return; }
    if ($parameter > 0)   { return; }

    <weak_warning descr="Senseless statement: return null implicitly or safely remove it.">return;</weak_warning>
}

function static_variables($parameter) {
    static $one, $two = null;
    if ($parameter) {
        return $one = $parameter;
    } else {
        return $two = $parameter;
    }
}