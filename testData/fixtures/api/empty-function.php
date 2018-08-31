<?php

    /* pattern: count can be used */
    echo <warning descr="You should probably use 'count([]) === 0' instead.">empty([])</warning>;
    echo <warning descr="You should probably use 'count([]) !== 0' instead.">!empty([])</warning>;

    function typedParams(?int $int, ?float $float, ?bool $boolean, ?string $string) {
        return [
            /* pattern: can be compared to null */
            <warning descr="You should probably use '$int === null' instead.">empty($int)</warning>,
            <warning descr="You should probably use '$float === null' instead.">empty($float)</warning>,
            <warning descr="You should probably use '$boolean === null' instead.">empty($boolean)</warning>,
            <warning descr="You should probably use '$string === null' instead.">empty($string)</warning>,
        ];
    }

    function getNullableInt(?int $int) { return $int; }
    echo <warning descr="You should probably use 'getNullableInt() === null' instead.">empty(getNullableInt())</warning>;
    echo <warning descr="You should probably use 'getNullableInt() !== null' instead.">!empty(getNullableInt())</warning>;

    echo <weak_warning descr="'empty(...)' counts too many values as empty, consider refactoring with type sensitive checks.">empty(1)</weak_warning>;
    echo <weak_warning descr="'empty(...)' counts too many values as empty, consider refactoring with type sensitive checks.">empty('...')</weak_warning>;
    echo <weak_warning descr="'empty(...)' counts too many values as empty, consider refactoring with type sensitive checks.">empty(null)</weak_warning>;
