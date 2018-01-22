<?php

function cases_holder() {
    $x = <warning descr="'(int) $y' should be used instead (up to 6x times faster).">intval($y)</warning>;
    $x = <warning descr="'(float) $y' should be used instead (up to 6x times faster).">floatval($y)</warning>;
    $x = <warning descr="'(string) $y' should be used instead (up to 6x times faster).">strval($y)</warning>;
    $x = <warning descr="'(bool) $y' should be used instead (up to 6x times faster).">boolval($y)</warning>;

    $x = intval(1 / 2, 16);

    <warning descr="'$x = (int) $x' should be used instead (up to 6x times faster).">settype($x, 'int')</warning>;
    <warning descr="'$x = (float) $x' should be used instead (up to 6x times faster).">settype($x, 'float')</warning>;
    <warning descr="'$x = (bool) $x' should be used instead (up to 6x times faster).">settype($x, 'bool')</warning>;
    <warning descr="'$x = (string) $x' should be used instead (up to 6x times faster).">settype($x, 'string')</warning>;
    <warning descr="'$x = (array) $x' should be used instead (up to 6x times faster).">settype($x, 'array')</warning>;

    settype($x, $x);
    settype($x, 'whatever');

    $x = <warning descr="'(string) ($y)' should be used instead (up to 6x times faster).">"{$y}"</warning>;
    $x = <warning descr="'(string) $y' should be used instead (up to 6x times faster).">"$y"</warning>;

    $x = " $y";
    $x = "$y ";
    $x = "{$y}{$y}";
}