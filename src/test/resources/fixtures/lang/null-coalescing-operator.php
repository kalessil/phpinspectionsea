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

    /* patter: isset on static properties was not working, fixed in PHP 7.0.19 and 7.1.5 */
    echo <weak_warning descr="'stdClass::$test ?? 'test'' construction should be used instead.">isset(stdClass::$test) ? stdClass::$test : 'test'</weak_warning>;
    echo <weak_warning descr="'$classname::$test ?? 'test'' construction should be used instead.">isset($classname::$test) ? $classname::$test : 'test'</weak_warning>;

    /* false-positives */
    echo array_key_exists(0, $x) ? $x[0] : 'default';
    echo isset($x[0], $s[1]) ? $x[0] : 'default';
    echo isset($x[0]) ? $x[0]->x : 'default';
    echo isset($x[0], $x[0]) ? $x[0] : 'default';