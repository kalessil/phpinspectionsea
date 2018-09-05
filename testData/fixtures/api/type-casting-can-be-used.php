<?php

function cases_holder() {
    $x = <warning descr="'(int) $y' would be more performant here (up to 6x times faster).">intval($y)</warning>;
    $x = <warning descr="'(float) $y' would be more performant here (up to 6x times faster).">floatval($y)</warning>;
    $x = <warning descr="'(string) $y' would be more performant here (up to 6x times faster).">strval($y)</warning>;
    $x = <warning descr="'(bool) $y' would be more performant here (up to 6x times faster).">boolval($y)</warning>;

    $x = <warning descr="'(int) ($y ?? 1)' would be more performant here (up to 6x times faster).">intval($y ?? 1)</warning>;
    $x = <warning descr="'(int) ($y ?: 1)' would be more performant here (up to 6x times faster).">intval($y ?: 1)</warning>;

    $x = intval(1 / 2, 16);

    <warning descr="'$x = (int) $x' would be more performant here (up to 6x times faster).">settype($x, 'int')</warning>;
    <warning descr="'$x = (float) $x' would be more performant here (up to 6x times faster).">settype($x, 'float')</warning>;
    <warning descr="'$x = (bool) $x' would be more performant here (up to 6x times faster).">settype($x, 'bool')</warning>;
    <warning descr="'$x = (string) $x' would be more performant here (up to 6x times faster).">settype($x, 'string')</warning>;
    <warning descr="'$x = (array) $x' would be more performant here (up to 6x times faster).">settype($x, 'array')</warning>;

    settype($x, $x);
    settype($x, 'whatever');

    $x = <warning descr="'(string) ($y)' would express the intention here better (less types coercion magic).">"{$y}"</warning>;
    $x = <warning descr="'(string) $y' would express the intention here better (less types coercion magic).">"$y"</warning>;

    $x = " $y";
    $x = "$y ";
    $x = "{$y}{$y}";

    $x = <warning descr="Casting to int or float would be more performant here (up to 6x times faster).">$x * 1</warning>;
    $x = <warning descr="Casting to int or float would be more performant here (up to 6x times faster).">1 * $x</warning>;

    $x = $x * -1;
}