<?php
    class Clazz {
        /** @var string */
        public $string;

        /** @var null|Clazz */
        public $nullable;
    }
    /** @var Clazz $object */
    /** @var Clazz[] $array */

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
    echo $object->nullable ?? null;
    echo $object->string ?? null;
    echo $array[$index]->string ?? null;
    echo $object->nullable->string ?? null;
    echo $object->string ?? null;
    echo $array[$index]->property ?? null;
    echo $object->string->property ?? null;
    echo $object ? $object->nullable : '...';
    echo $unknown ? $unknown->nullable : '...';

    /* false-positives */
    echo array_key_exists(0, $x) ? $x[0] : 'default';
    echo isset($x[0], $s[1]) ? $x[0] : 'default';
    echo isset($x[0]) ? $x[0]->x : 'default';
    echo isset($x[0], $x[0]) ? $x[0] : 'default';