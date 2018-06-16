<?php

    /* pattern: count can be used */
    echo 0 === count([]);
    echo 0 !== count([]);

    function typedParams(?int $int, ?float $float, ?bool $boolean) {
        return [
            /* pattern: can be compared to null */
            null === $int,
            null === $float,
            null === $boolean
        ];
    }

    function getNullableInt(?int $int) { return $int; }
    echo null === getNullableInt();
    echo null !== getNullableInt();

    echo empty(1);
    echo empty('...');
    echo empty(null);
