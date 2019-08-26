<?php

function cases_holder($one, $two) {
    return [
        <error descr="This call compares the same string with itself, this can not be right.">hash_equals($one, $one)</error>,

        /* false-positives: different arguments */
        hash_equals($one, $two),
    ];
}