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
            return '...';
        }
    };

    return $lambda;
}

function multiple_return_statements($parameter) {
    if (0 === $parameter) { return; }
    if ($parameter > 0)   { return; }

    return;
}

function static_variables($parameter) {
    static $one, $two = null;
    if ($parameter) {
        return $one = $parameter;
    } else {
        return $two = $parameter;
    }
}