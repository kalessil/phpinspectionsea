<?php

function cases_holder() {
    return [
        strtolower(substr('', 0, -1)) == '.',
        strtolower(substr('', 0, -1)) != '.',
        strtolower(substr('', 0, 1)) === '.',
        strtolower(substr('', 0, 1)) !== '.',

        substr('', 0, -1) == '.',
        substr('', 0, 1) === '.',

        substr('', -1) == '.',
        substr('', 3) === '.',
        substr('', -1) === 'a',
        substr('', 3) === 'a',

        substr('...', 0, -3) == '...',
        substr('...', 0, 3) === '...',

        /* workaround for https://youtrack.jetbrains.com/issue/WI-44824 */
        substr('...', 0, -1) == "\r",
        substr('...', 0, -1) == "\r",

        /* false-positives: edge cases */
        substr('...', 3) === '',  /* string length check tolerant to input, works in PHP 7.0+ */
        substr('ё..', 2) === 'ё', /* multi-byte magic */
    ];
}
