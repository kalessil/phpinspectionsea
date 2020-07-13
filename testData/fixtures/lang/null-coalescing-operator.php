<?php

    /* pattern: isset */
    echo <weak_warning descr="[EA] '$x[0] ?? null' can be used instead (reduces cognitive load).">isset($x[0]) ? $x[0] : null</weak_warning>;
    echo <weak_warning descr="[EA] '$x[0] ?? null' can be used instead (reduces cognitive load).">!isset($x[0]) ? null : $x[0]</weak_warning>;

    /* pattern: null comparision */
    echo <weak_warning descr="[EA] '$x[0] ?? 'alternative'' can be used instead (reduces cognitive load).">$x[0] !== null ? $x[0] : 'alternative'</weak_warning>;
    echo <weak_warning descr="[EA] '$x[0] ?? 'alternative'' can be used instead (reduces cognitive load).">null !== $x[0] ? $x[0] : 'alternative'</weak_warning>;
    echo <weak_warning descr="[EA] '$x[0] ?? 'alternative'' can be used instead (reduces cognitive load).">$x[0] === null ? 'alternative' : $x[0]</weak_warning>;
    echo <weak_warning descr="[EA] '$x[0] ?? 'alternative'' can be used instead (reduces cognitive load).">null === $x[0] ? 'alternative' : $x[0]</weak_warning>;

    /* pattern: array_key_exists with alternative null */
    echo <weak_warning descr="[EA] '$x[0] ?? null' can be used instead (reduces cognitive load).">array_key_exists(0, $x) ? $x[0] : null</weak_warning>;
    echo <weak_warning descr="[EA] '$x[0] ?? null' can be used instead (reduces cognitive load).">!array_key_exists(0, $x) ? null : $x[0]</weak_warning>;

    /* patter: isset on static properties was not working, fixed in PHP 7.0.19 and 7.1.5 */
    echo <weak_warning descr="[EA] 'stdClass::$test ?? 'test'' can be used instead (reduces cognitive load).">isset(stdClass::$test) ? stdClass::$test : 'test'</weak_warning>;
    echo <weak_warning descr="[EA] '$classname::$test ?? 'test'' can be used instead (reduces cognitive load).">isset($classname::$test) ? $classname::$test : 'test'</weak_warning>;

    /* pattern: not-empty ? property-reference : alternative */
    echo <weak_warning descr="[EA] '$object->property ?? null' can be used instead (reduces cognitive load).">$object ? $object->property : null</weak_warning>;
    echo <weak_warning descr="[EA] '$array[$index]->property ?? null' can be used instead (reduces cognitive load).">$array[$index] ? $array[$index]->property : null</weak_warning>;
    echo <weak_warning descr="[EA] '$object->property->property ?? null' can be used instead (reduces cognitive load).">$object->property ? $object->property->property : null</weak_warning>;
    echo <weak_warning descr="[EA] '$object->property ?? null' can be used instead (reduces cognitive load).">!empty($object) ? $object->property : null</weak_warning>;
    echo <weak_warning descr="[EA] '$array[$index]->property ?? null' can be used instead (reduces cognitive load).">!empty($array[$index]) ? $array[$index]->property : null</weak_warning>;
    echo <weak_warning descr="[EA] '$object->property->property ?? null' can be used instead (reduces cognitive load).">!empty($object->property) ? $object->property->property : null</weak_warning>;

    /* false-positives */
    echo array_key_exists(0, $x) ? $x[0] : 'default';
    echo isset($x[0], $s[1]) ? $x[0] : 'default';
    echo isset($x[0]) ? $x[0]->x : 'default';
    echo isset($x[0], $x[0]) ? $x[0] : 'default';