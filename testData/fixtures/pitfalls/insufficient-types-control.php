<?php

function cases_holder($nullable = null, $falsy = false) {
    $index = array_search('...', []);

    $condition = <warning descr="[EA] In multiple cases the result can be evaluated as false, consider hardening the condition.">array_search</warning>('...', []) &&
                 <warning descr="[EA] In multiple cases the result can be evaluated as false, consider hardening the condition.">strpos</warning>('...', '...') &&
                 array_search('...', []) !== false &&
                 strpos('...', '...') !== false;

    return [
        <warning descr="[EA] In multiple cases the result can be evaluated as false, consider hardening the condition.">$nullable < 5</warning>,
        <warning descr="[EA] In multiple cases the result can be evaluated as false, consider hardening the condition.">$nullable <= 5</warning>,
        <warning descr="[EA] In multiple cases the result can be evaluated as false, consider hardening the condition.">5 > $nullable</warning>,
        <warning descr="[EA] In multiple cases the result can be evaluated as false, consider hardening the condition.">5 >= $nullable</warning>,
    ];
}