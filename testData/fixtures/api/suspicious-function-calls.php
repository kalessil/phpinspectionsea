<?php

function cases_holder($one, $two) {
    return [
        <error descr="[EA] Identical arguments has been dispatched to this call, this can not be right.">hash_equals($one, $one)</error>,
        <error descr="[EA] Identical arguments has been dispatched to this call, this can not be right.">array_merge(['...'], ['...'])</error>,

        /* false-positives: different arguments */
        hash_equals($one, $two),
    ];
}