<?php

class Clazz {
    public function returnsInt(): int {}
    public function returnsFloat(): float {}
}

function returns_int(): int {}
function returns_float(): float {}

function cases_holder(string $parameter, Clazz $object) {
    return [
        <error descr="'is_numeric((int)$parameter)' seems to be always true.">is_numeric((int)$parameter)</error>,
        <error descr="'is_numeric((float)$parameter)' seems to be always true.">is_numeric((float)$parameter)</error>,
        is_numeric($parameter),
        is_numeric((string)$parameter),

        <error descr="'is_numeric(returns_int())' seems to be always true.">is_numeric(returns_int())</error>,
        <error descr="'is_numeric(returns_float())' seems to be always true.">is_numeric(returns_float())</error>,
        <error descr="'is_numeric($object->returnsInt())' seems to be always true.">is_numeric($object->returnsInt())</error>,
        <error descr="'is_numeric($object->returnsFloat())' seems to be always true.">is_numeric($object->returnsFloat())</error>,
    ];
}