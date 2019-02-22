<?php

    /* pattern: count can be used */
    echo count([]) === 0;
    echo count([]) !== 0;

    /**
     * @param int|null $int
     * @param float|null $float
     * @param bool|null $boolean
     * @param resource|null $resource
     * @param null|string $string
     */
    function typedParams($int, $float, $boolean, $resource, $string) {
        return [
            /* pattern: can be compared to null */
            $int === null,
            $float === null,
            $boolean === null,
            $resource === null,
            empty($string),
        ];
    }

    function getNullableInt(?int $int) { return $int; }
    echo getNullableInt() === null;
    echo getNullableInt() !== null;

    echo empty(1);
    echo empty('...');
    echo empty(null);

    function empty_with_fields(object $subject) {
        return [
            empty($subject->b),
            empty($subject->b->c),
            empty($subject->b[0]->c),
            empty($subject->method()->c),
        ];
    }
