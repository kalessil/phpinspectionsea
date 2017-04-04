<?php

    /* pattern: count can be used */
    echo 0 === count([]);
    echo 0 !== count([]);

    function typedParams(?int $int, ?float $float, ?string $string, ?bool $boolean) {
        return [
            /* pattern: can be compared to null */
            null === $int,
            null === $float,
            null === $string,
            null === $boolean
        ];
    }

    function getNullableInt(?int $int)          { return $int; }
    function getNullableString(?string $string) { return $string; }
    echo null === getNullableInt();
    echo null !== getNullableInt();
    echo null === getNullableString();
    echo null !== getNullableString();

    echo empty(1);
    echo empty(null);
