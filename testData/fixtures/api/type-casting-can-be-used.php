<?php

function cases_holder() {
    $x = <weak_warning descr="'(int) $y' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">intval($y)</weak_warning>;
    $x = <weak_warning descr="'(float) $y' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">floatval($y)</weak_warning>;
    $x = <weak_warning descr="'(string) $y' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">strval($y)</weak_warning>;
    $x = <weak_warning descr="'(bool) $y' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">boolval($y)</weak_warning>;

    $x = <weak_warning descr="'(int) ($y ?? 1)' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">intval($y ?? 1)</weak_warning>;
    $x = <weak_warning descr="'(int) ($y ?: 1)' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">intval($y ?: 1)</weak_warning>;

    $x = intval(1 / 2, 16);

    <weak_warning descr="'$x = (int) $x' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">settype($x, 'int')</weak_warning>;
    <weak_warning descr="'$x = (float) $x' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">settype($x, 'float')</weak_warning>;
    <weak_warning descr="'$x = (bool) $x' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">settype($x, 'bool')</weak_warning>;
    <weak_warning descr="'$x = (string) $x' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">settype($x, 'string')</weak_warning>;
    <weak_warning descr="'$x = (array) $x' can be used instead (reduces cognitive load, up to 6x times faster in PHP 5.x).">settype($x, 'array')</weak_warning>;

    settype($x, $x);
    settype($x, 'whatever');

    $x = <weak_warning descr="'(string) ($y)' would express the intention here better (less types coercion magic).">"{$y}"</weak_warning>;
    $x = <weak_warning descr="'(string) $y' would express the intention here better (less types coercion magic).">"$y"</weak_warning>;

    $x = " $y";
    $x = "$y ";
    $x = "{$y}{$y}";

    $x = <weak_warning descr="'(string) $x' would express the intention here better.">$x->__toString()</weak_warning>;

    $x = <weak_warning descr="Casting to int or float would be more performant here (up to 6x times faster).">$x * 1</weak_warning>;
    $x = <weak_warning descr="Casting to int or float would be more performant here (up to 6x times faster).">1 * $x</weak_warning>;

    $x = $x * -1;
}