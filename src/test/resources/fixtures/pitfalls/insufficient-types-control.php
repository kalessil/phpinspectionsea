<?php

function cases_holder() {
    $local =[
        array_search('...', []),
        strpos('...', '...')
    ];

    return
        <warning descr="In multiple cases the result can be evaluated as false, consider hardening the check (e.g. '... !== false').">array_search</warning>('...', []) &&
        <warning descr="In multiple cases the result can be evaluated as false, consider hardening the check (e.g. '... !== false').">strpos</warning>('...', '...') &&
        array_search('...', []) !== false &&
        strpos('...', '...') !== false
        ;
}