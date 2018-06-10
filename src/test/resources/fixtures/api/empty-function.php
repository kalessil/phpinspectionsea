<?php

    /* pattern: count can be used */
    echo <warning descr="You should probably use '0 === count([])' instead.">empty([])</warning>;
    echo <warning descr="You should probably use '0 !== count([])' instead.">!empty([])</warning>;

    function typedParams(?int $int, ?float $float, ?bool $boolean) {
        return [
            /* pattern: can be compared to null */
            <warning descr="You should probably use 'null === $int' instead.">empty($int)</warning>,
            <warning descr="You should probably use 'null === $float' instead.">empty($float)</warning>,
            <warning descr="You should probably use 'null === $boolean' instead.">empty($boolean)</warning>
        ];
    }

    function getNullableInt(?int $int) { return $int; }
    echo <warning descr="You should probably use 'null === getNullableInt()' instead.">empty(getNullableInt())</warning>;
    echo <warning descr="You should probably use 'null !== getNullableInt()' instead.">!empty(getNullableInt())</warning>;

    echo <weak_warning descr="'empty(...)' counts too many values as empty, consider refactoring with type sensitive checks.">empty(1)</weak_warning>;
    echo <weak_warning descr="'empty(...)' counts too many values as empty, consider refactoring with type sensitive checks.">empty('...')</weak_warning>;
    echo <weak_warning descr="'empty(...)' counts too many values as empty, consider refactoring with type sensitive checks.">empty(null)</weak_warning>;
