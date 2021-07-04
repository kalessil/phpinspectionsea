<?php

function cases_holder($boundParameter, $unboundParameter) {
    $boundLocal = null;
    $lambda = function ($regular, & $reference) use (& $boundParameter, & $boundLocal) {
        if ($boundParameter === null) {
            return $boundParameter = '...';
        }
        if ($boundLocal === null) {
            return $boundLocal = '...';
        }
        if ($reference === null) {
            return $reference = '...';
        }
        if ($regular === null) {
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

function used_in_finally() {
    try {
        return $variable = '...';
    } finally {
        hook($variable);
    }
}