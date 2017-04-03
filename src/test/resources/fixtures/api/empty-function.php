<?php

echo <warning descr="'0 === count($...)' construction should be used instead.">empty([])</warning>;

function getNullableInt(?int $int) {
    return $int;
}

function getNullableString(?string $string) {
    return $string;
}

function isIntNull(?int $int) {
    return <warning descr="You should probably use 'null === $...'.">empty($int)</warning>;
}

echo <warning descr="You should probably use 'null === $...'.">empty(getNullableInt())</warning>;
echo <warning descr="You should probably use 'null === $...'.">empty(getNullableString())</warning>;

echo <weak_warning descr="'empty(...)' counts too many values as empty, consider refactoring with type sensitive checks.">empty(1)</weak_warning>;
echo <weak_warning descr="'empty(...)' counts too many values as empty, consider refactoring with type sensitive checks.">empty(null)</weak_warning>;
