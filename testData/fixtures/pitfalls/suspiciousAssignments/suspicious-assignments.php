<?php

abstract class Clazz implements ArrayAccess {}

function array_destructuring(string $string, array $array, mixed $mixed, Clazz $clazz)
{
    list($a, $b) = $array;
    [$c, $d]     = $array;

    list($a, $b) = $mixed;
    [$c, $d]     = $mixed;

    <error descr="[EA] This assignment doesn't make any sense here, as the assigned value isn't an array.">list($e, $f) = $string</error>;
    <error descr="[EA] This assignment doesn't make any sense here, as the assigned value isn't an array.">[$h, $h]     = $string</error>;

    [$i, $j]     = $clazz;
    <error descr="[EA] This assignment doesn't make any sense here, as the assigned value isn't an array.">[$k, $l]     = new \stdClass()</error>;
}
