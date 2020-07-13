<?php

function cases_holder($condition, $positive, $negative) {
    return [
        ($condition && $condition) ? $positive : $negative,
        (($condition) && $condition) ? $positive : $negative,

        /* false-positives: mismatching alternatives, misplaced ternary */
        $condition ? ($condition ? $negative : $positive) : $negative,
        $condition ? $negative : ($condition ? $positive : $negative),
    ];
}