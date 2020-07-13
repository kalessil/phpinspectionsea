<?php

function cases_holder($condition, $positive, $negative) {
    return [
        <weak_warning descr="[EA] '($condition && $condition) ? ... : ...' can be used instead (reduces cognitive load, improves maintainability)">$condition ? ($condition ? $positive : $negative) : $negative</weak_warning>,
        <weak_warning descr="[EA] '(($condition) && $condition) ? ... : ...' can be used instead (reduces cognitive load, improves maintainability)">($condition) ? ($condition ? $positive : $negative) : $negative</weak_warning>,

        /* false-positives: mismatching alternatives, misplaced ternary */
        $condition ? ($condition ? $negative : $positive) : $negative,
        $condition ? $negative : ($condition ? $positive : $negative),
    ];
}