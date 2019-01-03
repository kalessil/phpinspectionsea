<?php

function cases_holder(bool $boolean, string $string) {
    return [
        $boolean,
        $boolean,
        !$boolean,
        !$boolean,
        !$boolean,
        !$boolean,
        $boolean,
        $boolean,

        /* false-positives: non-booleans, weak operators */
        $string === true,
        $boolean == true,
        $boolean != false,
    ];
}