<?php

    /* pattern: isset */
    echo <weak_warning descr="'$x[0] ?? null' construction should be used instead.">isset($x[0]) ? $x[0] : null</weak_warning>;
    echo <weak_warning descr="'$x[0] ?? null' construction should be used instead.">!isset($x[0]) ? null : $x[0]</weak_warning>;

    /* pattern: null comparision */
    echo <weak_warning descr="'$x[0] ?? 'alternative'' construction should be used instead.">$x[0] !== null ? $x[0] : 'alternative'</weak_warning>;
    echo <weak_warning descr="'$x[0] ?? 'alternative'' construction should be used instead.">null !== $x[0] ? $x[0] : 'alternative'</weak_warning>;
    echo <weak_warning descr="'$x[0] ?? 'alternative'' construction should be used instead.">$x[0] === null ? 'alternative' : $x[0]</weak_warning>;
    echo <weak_warning descr="'$x[0] ?? 'alternative'' construction should be used instead.">null === $x[0] ? 'alternative' : $x[0]</weak_warning>;

    /* pattern: array_key_exists with alternative null */
    echo <weak_warning descr="'$x[0] ?? null' construction should be used instead.">array_key_exists(0, $x) ? $x[0] : null</weak_warning>;
    echo <weak_warning descr="'$x[0] ?? null' construction should be used instead.">!array_key_exists(0, $x) ? null : $x[0]</weak_warning>;

    /* false-positives */
    echo array_key_exists(0, $x) ? $x[0] : 'default';
    echo isset($x[0], $s[1]) ? $x[0] : 'default';
    echo isset($x[0]) ? $x[0]->x : 'default';
    echo isset($x[0], $x[0]) ? $x[0] : 'default';

    /* false-positives: refactoring causes errors */
    echo isset(stdClass::$test) ? stdClass::$test : 'test';
    echo isset($classname::$test) ? $classname::$test : 'test';