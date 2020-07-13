<?php

    /* pattern: isset */
    echo $x[0] ?? null;
    echo $x[0] ?? null;

    /* pattern: null comparision */
    echo $x[0] ?? 'alternative';
    echo $x[0] ?? 'alternative';
    echo $x[0] ?? 'alternative';
    echo $x[0] ?? 'alternative';

    /* pattern: array_key_exists with alternative null */
    echo $x[0] ?? null;
    echo $x[0] ?? null;

    /* patter: isset on static properties was not working, fixed in PHP 7.0.19 and 7.1.5 */
    echo stdClass::$test ?? 'test';
    echo $classname::$test ?? 'test';

    /* pattern: not-empty ? property-reference : alternative */
    echo $object->property ?? null;
    echo $array[$index]->property ?? null;
    echo $object->property->property ?? null;

    /* false-positives */
    echo array_key_exists(0, $x) ? $x[0] : 'default';
    echo isset($x[0], $s[1]) ? $x[0] : 'default';
    echo isset($x[0]) ? $x[0]->x : 'default';
    echo isset($x[0], $x[0]) ? $x[0] : 'default';