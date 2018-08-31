<?php

    /* pattern: count can be used */
    echo count([]) === 0;
    echo count([]) !== 0;

    function typedParams(?int $int, ?float $float, ?bool $boolean, ?string $string) {
        return [
            /* pattern: can be compared to null */
            $int === null,
            $float === null,
            $boolean === null,
            $string === null,
        ];
    }

    function getNullableInt(?int $int) { return $int; }
    echo getNullableInt() === null;
    echo getNullableInt() !== null;

    echo empty(1);
    echo empty('...');
    echo empty(null);
